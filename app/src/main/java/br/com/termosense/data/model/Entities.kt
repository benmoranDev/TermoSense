package br.com.termosense.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_units")
data class HealthUnitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val profileType: String, // "VACINAS_PNI", "MEDICAMENTOS", "HEMODERIVADOS", "ESTUFA_LAB"
    val minTemp: Double = 2.0,
    val maxTemp: Double = 8.0,
    val minHumidity: Double = 35.0,
    val maxHumidity: Double = 65.0,
    val location: String,
    val address: String = "São Paulo, SP - SUS",
    val isOfflineMode: Boolean = false,
    val coolerRelayPin: String = "GPIO 26",
    val isCoolerActive: Boolean = true
)

@Entity(tableName = "device_nodes")
data class DeviceNodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: Long,
    val name: String,
    val model: String = "ESP32-S3",
    val sensorType: String = "DS18B20",
    val ipAddress: String = "192.168.1.145",
    val macAddress: String = "24:6F:28:AB:12:89",
    val firmwareVersion: String = "v2.4.1-ota",
    val samplingRateMs: Long = 2000,
    val isOnline: Boolean = true,
    val currentTemp: Double = 4.8,
    val currentHumidity: Double = 52.0,
    val currentDewPoint: Double = 1.2,
    val vccRail: Double = 3.32,
    val rssi: Int = -54,
    val uptimeString: String = "18d 04h 12m",
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "telemetry_readings")
data class TelemetryReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: Long,
    val deviceId: Long,
    val temperature: Double,
    val humidity: Double,
    val dewPoint: Double,
    val vcc: Double,
    val rssi: Int,
    val isExcursion: Boolean,
    val isOfflineBuffered: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "alert_events")
data class AlertEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: Long,
    val deviceId: Long,
    val title: String,
    val message: String,
    val severity: String, // "CRITICAL", "WARNING", "INFO"
    val currentTemp: Double,
    val limitTemp: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false,
    val isSilenced: Boolean = false,
    val nodeLabel: String = "ESP32 Sala de Vacinas"
)

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: Long,
    val name: String,
    val conditionText: String,
    val actionText: String,
    val isEnabled: Boolean = true,
    val activationsCount: Int = 0,
    val targetType: String = "RELAY_COOLER" // "RELAY_COOLER", "ALERT_HUMIDITY", "WATCHDOG_PING"
)

@Entity(tableName = "compliance_reports")
data class ComplianceReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: Long,
    val unitName: String,
    val monthPeriod: String, // e.g. "Setembro / 2026"
    val responsibleTechnician: String,
    val professionalRegistry: String, // e.g. "CRF/SP 48.912" or "COREN-SP 182.490"
    val minTempRecorded: Double,
    val maxTempRecorded: Double,
    val meanTempRecorded: Double,
    val meanKineticTemp: Double,
    val minHumidityRecorded: Double,
    val maxHumidityRecorded: Double,
    val totalReadings: Int,
    val excursionMinutes: Int,
    val compliancePercentage: Double,
    val generatedAt: Long = System.currentTimeMillis(),
    val observations: String = "Conforme RDC ANVISA nº 430/2020 e Manual de Rede de Frio do PNI/MS."
)

@Entity(tableName = "offline_telemetry_cache")
data class OfflineTelemetryCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: Long,
    val deviceId: Long,
    val deviceName: String = "ESP32 TermoSense Node",
    val temperature: Double,
    val humidity: Double,
    val dewPoint: Double,
    val vcc: Double = 3.32,
    val rssi: Int = 0,
    val isExcursion: Boolean = false,
    val capturedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val syncedAt: Long? = null,
    val captureReason: String = "LOSS_OF_CONNECTION" // "LOSS_OF_CONNECTION", "BACKGROUND_BUFFER", "ESP32_INTERNAL_FLASH", "MANUAL_CONTINGENCY"
)

data class SyncSummaryState(
    val lastSyncCount: Int = 0,
    val lastSyncTimestamp: Long = 0L,
    val isSyncing: Boolean = false,
    val isAutoSyncEnabled: Boolean = true,
    val message: String? = null
)
