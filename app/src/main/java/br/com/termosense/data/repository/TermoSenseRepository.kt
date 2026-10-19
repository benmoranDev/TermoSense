package br.com.termosense.data.repository

import android.content.Context
import br.com.termosense.data.local.AppDatabase
import br.com.termosense.data.model.AlertEventEntity
import br.com.termosense.data.model.AutomationRuleEntity
import br.com.termosense.data.model.ComplianceReportEntity
import br.com.termosense.data.model.DeviceNodeEntity
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.data.model.OfflineTelemetryCacheEntity
import br.com.termosense.data.model.TelemetryReadingEntity
import br.com.termosense.service.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlin.math.ln

class TermoSenseRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context)
) {
    private val unitDao = database.healthUnitDao()
    private val deviceDao = database.deviceNodeDao()
    private val telemetryDao = database.telemetryDao()
    private val alertDao = database.alertDao()
    private val ruleDao = database.automationRuleDao()
    private val reportDao = database.complianceReportDao()
    private val offlineCacheDao = database.offlineCacheDao()
    private val notificationHelper = NotificationHelper(context)

    val allUnits: Flow<List<HealthUnitEntity>> = unitDao.getAllUnits()
    val allDevices: Flow<List<DeviceNodeEntity>> = deviceDao.getAllDevices()
    val activeAlerts: Flow<List<AlertEventEntity>> = alertDao.getActiveAlerts()
    val activeAlertsCount: Flow<Int> = alertDao.getActiveAlertsCount()
    val allAlerts: Flow<List<AlertEventEntity>> = alertDao.getAllAlerts()
    val allRules: Flow<List<AutomationRuleEntity>> = ruleDao.getAllRules()
    val allReports: Flow<List<ComplianceReportEntity>> = reportDao.getAllReports()

    val unsyncedCacheCount: Flow<Int> = offlineCacheDao.getUnsyncedCount()
    val unsyncedCachedReadings: Flow<List<OfflineTelemetryCacheEntity>> = offlineCacheDao.getUnsyncedReadingsFlow()
    val allCachedReadings: Flow<List<OfflineTelemetryCacheEntity>> = offlineCacheDao.getAllCachedReadings()

    fun getUnitById(id: Long): Flow<HealthUnitEntity?> = unitDao.getUnitByIdFlow(id)
    fun getDevicesByUnit(unitId: Long): Flow<List<DeviceNodeEntity>> = deviceDao.getDevicesByUnit(unitId)
    fun getRecentReadings(unitId: Long): Flow<List<TelemetryReadingEntity>> = telemetryDao.getRecentReadings(unitId, 150)
    fun getReadingsSince(unitId: Long, since: Long): Flow<List<TelemetryReadingEntity>> = telemetryDao.getReadingsSince(unitId, since)
    fun getLatestReading(unitId: Long): Flow<TelemetryReadingEntity?> = telemetryDao.getLatestReading(unitId)
    fun getUnsyncedCacheCountByUnit(unitId: Long): Flow<Int> = offlineCacheDao.getUnsyncedCountByUnit(unitId)

    suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        // Production mode: No fake pre-seeded units or devices.
        // Database starts empty for real production data collection.
    }

    suspend fun recordLiveReading(
        unitId: Long,
        deviceId: Long,
        temp: Double,
        humidity: Double,
        vcc: Double = 3.32,
        rssi: Int = -54
    ) = withContext(Dispatchers.IO) {
        val unit = unitDao.getUnitById(unitId) ?: return@withContext
        val isTempExcursion = temp < unit.minTemp || temp > unit.maxTemp
        val isHumidityExcursion = humidity < unit.minHumidity || humidity > unit.maxHumidity
        val isExcursion = isTempExcursion || isHumidityExcursion
        val dewPoint = calculateDewPoint(temp, humidity)

        val reading = TelemetryReadingEntity(
            unitId = unitId,
            deviceId = deviceId,
            temperature = temp,
            humidity = humidity,
            dewPoint = Math.round(dewPoint * 10.0) / 10.0,
            vcc = vcc,
            rssi = rssi,
            isExcursion = isExcursion,
            isOfflineBuffered = unit.isOfflineMode,
            timestamp = System.currentTimeMillis()
        )
        telemetryDao.insertReading(reading)

        // Update device node live status
        val dev = deviceDao.getDeviceById(deviceId)
        if (dev != null) {
            val updatedDev = dev.copy(
                currentTemp = temp,
                currentHumidity = humidity,
                currentDewPoint = Math.round(dewPoint * 10.0) / 10.0,
                lastSeenTimestamp = System.currentTimeMillis()
            )
            deviceDao.updateDevice(updatedDev)
        }

        // Check Temperature Threshold
        if (isTempExcursion) {
            val isOver = temp > unit.maxTemp
            val alert = AlertEventEntity(
                unitId = unitId,
                deviceId = deviceId,
                title = if (isOver) "🚨 Temperatura Elevada: ${unit.code}" else "❄️ Sub-resfriamento Crítico: ${unit.code}",
                message = "${unit.name}: Leitura de ${String.format("%.1f", temp)}°C viola os limites (${unit.minTemp}°C a ${unit.maxTemp}°C)!",
                severity = "CRITICAL",
                currentTemp = temp,
                limitTemp = if (isOver) unit.maxTemp else unit.minTemp,
                timestamp = System.currentTimeMillis(),
                nodeLabel = dev?.name ?: "ESP32 Sensor"
            )
            alertDao.insertAlert(alert)

            notificationHelper.sendThermalAlert(
                title = if (isOver) "🚨 Alerta: Temperatura Acima do Limite" else "❄️ Alerta: Sub-resfriamento",
                message = "${unit.name}: ${String.format("%.1f", temp)}°C detectado (Faixa permitida: ${unit.minTemp}°C a ${unit.maxTemp}°C).",
                isCritical = true,
                unitKey = "unit_${unit.id}_thermal",
                notificationId = (1000 + unit.id).toInt()
            )
        }

        // Check Humidity Threshold
        if (isHumidityExcursion) {
            val isOver = humidity > unit.maxHumidity
            val alert = AlertEventEntity(
                unitId = unitId,
                deviceId = deviceId,
                title = if (isOver) "💧 Umidade Crítica Elevada: ${unit.code}" else "⚠️ Umidade Muito Baixa: ${unit.code}",
                message = "${unit.name}: Umidade de ${String.format("%.1f", humidity)}% fora da faixa (${unit.minHumidity}% a ${unit.maxHumidity}% UR)!",
                severity = "WARNING",
                currentTemp = temp,
                limitTemp = if (isOver) unit.maxHumidity else unit.minHumidity,
                timestamp = System.currentTimeMillis(),
                nodeLabel = dev?.name ?: "Sensor DHT22"
            )
            alertDao.insertAlert(alert)

            notificationHelper.sendHumidityAlert(
                title = if (isOver) "💧 Alerta de Umidade Alta (Condensação)" else "⚠️ Alerta de Umidade Baixa",
                message = "${unit.name}: ${String.format("%.1f", humidity)}% UR (Faixa permitida: ${unit.minHumidity}% a ${unit.maxHumidity}% UR).",
                isCritical = false,
                unitKey = "unit_${unit.id}_humidity",
                notificationId = (2000 + unit.id).toInt()
            )
        }
    }

    suspend fun addNewUnit(unit: HealthUnitEntity): Long = withContext(Dispatchers.IO) {
        unitDao.insertUnit(unit)
    }

    suspend fun updateUnit(unit: HealthUnitEntity) = withContext(Dispatchers.IO) {
        unitDao.updateUnit(unit)
    }

    suspend fun deleteUnit(unit: HealthUnitEntity) = withContext(Dispatchers.IO) {
        unitDao.deleteUnit(unit)
    }

    suspend fun updateUnitThresholds(
        unitId: Long,
        minTemp: Double,
        maxTemp: Double,
        minHumidity: Double,
        maxHumidity: Double
    ) = withContext(Dispatchers.IO) {
        val unit = unitDao.getUnitById(unitId) ?: return@withContext
        val updated = unit.copy(
            minTemp = minTemp,
            maxTemp = maxTemp,
            minHumidity = minHumidity,
            maxHumidity = maxHumidity
        )
        unitDao.updateUnit(updated)
    }

    suspend fun toggleCooler(unitId: Long, isCoolerActive: Boolean) = withContext(Dispatchers.IO) {
        val unit = unitDao.getUnitById(unitId) ?: return@withContext
        val updated = unit.copy(isCoolerActive = isCoolerActive)
        unitDao.updateUnit(updated)
    }

    suspend fun toggleOfflineMode(unitId: Long, isOffline: Boolean) = withContext(Dispatchers.IO) {
        val unit = unitDao.getUnitById(unitId) ?: return@withContext
        val updated = unit.copy(isOfflineMode = isOffline)
        unitDao.updateUnit(updated)
    }

    suspend fun silenceAlert(alertId: Long) = withContext(Dispatchers.IO) {
        alertDao.silenceAlert(alertId)
    }

    suspend fun resolveAlert(alertId: Long) = withContext(Dispatchers.IO) {
        alertDao.resolveAlert(alertId)
    }

    suspend fun insertAlert(alert: AlertEventEntity): Long = withContext(Dispatchers.IO) {
        alertDao.insertAlert(alert)
    }

    suspend fun toggleRule(rule: AutomationRuleEntity) = withContext(Dispatchers.IO) {
        ruleDao.updateRule(rule.copy(isEnabled = !rule.isEnabled))
    }

    suspend fun addNewRule(rule: AutomationRuleEntity) = withContext(Dispatchers.IO) {
        ruleDao.insertRule(rule)
    }

    suspend fun addNewDevice(device: DeviceNodeEntity) = withContext(Dispatchers.IO) {
        deviceDao.insertDevice(device)
    }

    suspend fun deleteDevice(device: DeviceNodeEntity) = withContext(Dispatchers.IO) {
        deviceDao.deleteDevice(device)
    }

    suspend fun updateDeviceSamplingRate(deviceId: Long, rateMs: Long) = withContext(Dispatchers.IO) {
        val dev = deviceDao.getDeviceById(deviceId) ?: return@withContext
        deviceDao.updateDevice(dev.copy(samplingRateMs = rateMs))
    }

    suspend fun clearTelemetryByUnit(unitId: Long) = withContext(Dispatchers.IO) {
        telemetryDao.clearReadingsByUnit(unitId)
    }

    suspend fun clearEntireTelemetryHistory() = withContext(Dispatchers.IO) {
        telemetryDao.clearAllReadings()
    }

    suspend fun updateDeviceAddressAndProtocol(deviceId: Long, ipOrMac: String, isOnline: Boolean) = withContext(Dispatchers.IO) {
        val dev = deviceDao.getDeviceById(deviceId) ?: return@withContext
        val isIp = ipOrMac.contains(".")
        val updated = if (isIp) {
            dev.copy(ipAddress = ipOrMac, isOnline = isOnline, lastSeenTimestamp = System.currentTimeMillis())
        } else {
            dev.copy(macAddress = ipOrMac, isOnline = isOnline, lastSeenTimestamp = System.currentTimeMillis())
        }
        deviceDao.updateDevice(updated)
    }

    suspend fun updateDeviceTelemetry(deviceId: Long, temp: Double, hum: Double, rssi: Int, vcc: Double, isOnline: Boolean) = withContext(Dispatchers.IO) {
        val dev = deviceDao.getDeviceById(deviceId) ?: return@withContext
        val dewPoint = calculateDewPoint(temp, hum)
        val updated = dev.copy(
            currentTemp = temp,
            currentHumidity = hum,
            currentDewPoint = Math.round(dewPoint * 10.0) / 10.0,
            rssi = rssi,
            vccRail = vcc,
            isOnline = isOnline,
            lastSeenTimestamp = System.currentTimeMillis()
        )
        deviceDao.updateDevice(updated)
    }

    suspend fun insertComplianceReport(report: ComplianceReportEntity) = withContext(Dispatchers.IO) {
        reportDao.insertReport(report)
    }

    suspend fun getReadingsSinceList(unitId: Long, since: Long): List<TelemetryReadingEntity> = withContext(Dispatchers.IO) {
        telemetryDao.getReadingsSinceList(unitId, since)
    }

    suspend fun getUnit(unitId: Long): HealthUnitEntity? = withContext(Dispatchers.IO) {
        unitDao.getUnitById(unitId)
    }

    // =========================================================================
    //  ROOM OFFLINE CACHE & SYNCHRONIZATION LOGIC
    // =========================================================================

    suspend fun bufferOfflineReading(
        unitId: Long,
        deviceId: Long,
        temp: Double,
        hum: Double,
        vcc: Double = 3.32,
        rssi: Int = 0,
        deviceName: String = "ESP32 TermoSense Node",
        captureReason: String = "LOSS_OF_CONNECTION",
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val unit = unitDao.getUnitById(unitId)
        val isExcursion = if (unit != null) (temp < unit.minTemp || temp > unit.maxTemp) else false
        val dewPoint = calculateDewPoint(temp, hum)

        val cachedEntity = OfflineTelemetryCacheEntity(
            unitId = unitId,
            deviceId = deviceId,
            deviceName = deviceName,
            temperature = Math.round(temp * 10.0) / 10.0,
            humidity = Math.round(hum * 10.0) / 10.0,
            dewPoint = Math.round(dewPoint * 10.0) / 10.0,
            vcc = vcc,
            rssi = rssi,
            isExcursion = isExcursion,
            capturedAt = timestamp,
            isSynced = false,
            captureReason = captureReason
        )
        offlineCacheDao.insertCachedReading(cachedEntity)
    }

    suspend fun insertOfflineCacheBatch(readings: List<OfflineTelemetryCacheEntity>) = withContext(Dispatchers.IO) {
        offlineCacheDao.insertAllCachedReadings(readings)
    }

    suspend fun syncOfflineCacheToMainTelemetry(): Int = withContext(Dispatchers.IO) {
        val unsynced = offlineCacheDao.getUnsyncedReadingsList()
        if (unsynced.isEmpty()) return@withContext 0

        val convertedReadings = mutableListOf<TelemetryReadingEntity>()
        val unitsMap = mutableMapOf<Long, HealthUnitEntity?>()

        for (item in unsynced) {
            val unit = unitsMap.getOrPut(item.unitId) { unitDao.getUnitById(item.unitId) }
            val isExcursion = if (unit != null) (item.temperature < unit.minTemp || item.temperature > unit.maxTemp) else item.isExcursion

            convertedReadings.add(
                TelemetryReadingEntity(
                    unitId = item.unitId,
                    deviceId = item.deviceId,
                    temperature = item.temperature,
                    humidity = item.humidity,
                    dewPoint = item.dewPoint,
                    vcc = item.vcc,
                    rssi = item.rssi,
                    isExcursion = isExcursion,
                    isOfflineBuffered = true,
                    timestamp = item.capturedAt
                )
            )

            // Register retrospective alert if an excursion happened during the offline blackout
            if (isExcursion && unit != null) {
                alertDao.insertAlert(
                    AlertEventEntity(
                        unitId = unit.id,
                        deviceId = item.deviceId,
                        title = "⚠️ Excursão Térmica em Buffer Offline",
                        message = "Leitura registrada offline em ${unit.name}: ${item.temperature}°C (Limites: ${unit.minTemp}°C a ${unit.maxTemp}°C).",
                        severity = if (item.temperature > unit.maxTemp + 2.0 || item.temperature < unit.minTemp - 2.0) "CRITICAL" else "WARNING",
                        currentTemp = item.temperature,
                        limitTemp = if (item.temperature > unit.maxTemp) unit.maxTemp else unit.minTemp,
                        timestamp = item.capturedAt,
                        isResolved = false,
                        isSilenced = false,
                        nodeLabel = item.deviceName
                    )
                )
            }
        }

        // 1. Bulk insert converted readings into the active telemetry repository table
        telemetryDao.insertAllReadings(convertedReadings)

        // 2. Update latest device telemetry state with the most recent item in the batch
        val latestReading = unsynced.maxByOrNull { it.capturedAt }
        if (latestReading != null) {
            val dev = deviceDao.getDeviceById(latestReading.deviceId)
            if (dev != null) {
                deviceDao.updateDevice(
                    dev.copy(
                        currentTemp = latestReading.temperature,
                        currentHumidity = latestReading.humidity,
                        currentDewPoint = latestReading.dewPoint,
                        rssi = latestReading.rssi,
                        vccRail = latestReading.vcc,
                        isOnline = true,
                        lastSeenTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        // 3. Mark the offline cache records as synchronized in Room
        val now = System.currentTimeMillis()
        val ids = unsynced.map { it.id }
        offlineCacheDao.markAsSynced(ids, now)

        return@withContext unsynced.size
    }

    suspend fun clearOfflineCache() = withContext(Dispatchers.IO) {
        offlineCacheDao.clearAllCache()
    }

    private fun calculateDewPoint(temp: Double, humidity: Double): Double {
        val a = 17.27
        val b = 237.7
        val alpha = ((a * temp) / (b + temp)) + ln(humidity.coerceIn(1.0, 100.0) / 100.0)
        return (b * alpha) / (a - alpha)
    }
}
