package br.com.termosense.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.termosense.data.model.AlertEventEntity
import br.com.termosense.data.model.AutomationRuleEntity
import br.com.termosense.data.model.ComplianceReportEntity
import br.com.termosense.data.model.DeviceNodeEntity
import br.com.termosense.data.model.HealthUnitEntity
import br.com.termosense.data.model.OfflineTelemetryCacheEntity
import br.com.termosense.data.model.SyncSummaryState
import br.com.termosense.data.model.TelemetryReadingEntity
import br.com.termosense.data.repository.TermoSenseRepository
import br.com.termosense.service.NotificationHelper
import br.com.termosense.service.ReportExporter
import br.com.termosense.service.esp32.ConnectionStatus
import br.com.termosense.service.esp32.DiscoveredESP32
import br.com.termosense.service.esp32.ESP32ConnectionManager
import br.com.termosense.service.esp32.ESP32ConnectionState
import br.com.termosense.service.esp32.ESP32TelemetryPayload
import br.com.termosense.service.fcm.FCMTokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TimeframeFilter(val label: String, val durationMs: Long) {
    ONE_HOUR("1h", 3600 * 1000L),
    SIX_HOURS("6h", 6 * 3600 * 1000L),
    TWENTY_FOUR_HOURS("24h", 24 * 3600 * 1000L),
    SEVEN_DAYS("7d", 7 * 24 * 3600 * 1000L),
    THIRTY_DAYS("30d", 30 * 24 * 3600 * 1000L)
}

enum class MetricType(val label: String, val unit: String) {
    TEMPERATURE("Temperatura", "°C"),
    HUMIDITY("Umidade", "% UR"),
    COMBINED("Dual (Temp + Umidade)", "°C / %"),
    DEW_POINT("Sensação / P. Orvalho", "°C")
}

data class SerialLogItem(
    val timestamp: String,
    val message: String,
    val type: String // "INFO", "PUB", "WARN", "SYS"
)

class TermoSenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TermoSenseRepository(application)
    val esp32Manager = ESP32ConnectionManager(application, viewModelScope)
    val fcmTokenManager = FCMTokenManager(application)
    private val notificationHelper = NotificationHelper(application)

    // Firebase Cloud Messaging Push State
    val fcmToken: StateFlow<String?> = fcmTokenManager.fcmToken
    val subscribedTopics: StateFlow<Set<String>> = fcmTokenManager.subscribedTopics
    val isFcmReady: StateFlow<Boolean> = fcmTokenManager.isPushReady

    val allUnits: StateFlow<List<HealthUnitEntity>> = repository.allUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedUnitId = MutableStateFlow<Long>(1L)
    val selectedUnitId: StateFlow<Long> = _selectedUnitId.asStateFlow()

    val currentUnit: StateFlow<HealthUnitEntity?> = _selectedUnitId.flatMapLatest { id ->
        repository.getUnitById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentDevices: StateFlow<List<DeviceNodeEntity>> = _selectedUnitId.flatMapLatest { id ->
        repository.getDevicesByUnit(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDevices: StateFlow<List<DeviceNodeEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTimeframe = MutableStateFlow(TimeframeFilter.TWENTY_FOUR_HOURS)
    val selectedTimeframe: StateFlow<TimeframeFilter> = _selectedTimeframe.asStateFlow()

    private val _selectedMetric = MutableStateFlow(MetricType.COMBINED)
    val selectedMetric: StateFlow<MetricType> = _selectedMetric.asStateFlow()

    val recentReadings: StateFlow<List<TelemetryReadingEntity>> = _selectedUnitId.flatMapLatest { id ->
        repository.getRecentReadings(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAlerts: StateFlow<List<AlertEventEntity>> = repository.activeAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAlertsCount: StateFlow<Int> = repository.activeAlertsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allAlerts: StateFlow<List<AlertEventEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRules: StateFlow<List<AutomationRuleEntity>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<ComplianceReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Room Offline Cache & Auto-Sync State
    val unsyncedCacheCount: StateFlow<Int> = repository.unsyncedCacheCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allCachedReadings: StateFlow<List<OfflineTelemetryCacheEntity>> = repository.allCachedReadings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _syncState = MutableStateFlow(SyncSummaryState())
    val syncState: StateFlow<SyncSummaryState> = _syncState.asStateFlow()

    // Serial Terminal Logs State (Merged with real ESP32 logs)
    private val _serialLogs = MutableStateFlow<List<SerialLogItem>>(
        listOf(
            SerialLogItem("00:00:01", "Sistema TermoSense inicializado (Modo Produção)", "SYS"),
            SerialLogItem("00:00:02", "Banco de dados Room SQLite Ativo (0 leituras pendentes)", "SYS"),
            SerialLogItem("00:00:03", "Pronto para receber telemetria real via ESP32 BLE / Wi-Fi", "INFO")
        )
    )
    val serialLogs: StateFlow<List<SerialLogItem>> = _serialLogs.asStateFlow()

    // ESP32 Connection State
    val esp32ConnectionState: StateFlow<ESP32ConnectionState> = esp32Manager.connectionState
    val discoveredEsp32List: StateFlow<List<DiscoveredESP32>> = esp32Manager.discoveredDevices

    // Dialog & UI Action States
    private val _showPairingDialog = MutableStateFlow(false)
    val showPairingDialog: StateFlow<Boolean> = _showPairingDialog.asStateFlow()

    private val _showThresholdDialog = MutableStateFlow(false)
    val showThresholdDialog: StateFlow<Boolean> = _showThresholdDialog.asStateFlow()

    private val _showNewDeviceDialog = MutableStateFlow(false)
    val showNewDeviceDialog: StateFlow<Boolean> = _showNewDeviceDialog.asStateFlow()

    private val _showNewRuleDialog = MutableStateFlow(false)
    val showNewRuleDialog: StateFlow<Boolean> = _showNewRuleDialog.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _showNewSectorDialog = MutableStateFlow(false)
    val showNewSectorDialog: StateFlow<Boolean> = _showNewSectorDialog.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
        showToast(if (_isDarkMode.value) "Modo Escuro (Telemetria) ativado" else "Modo Claro (Clínico) ativado")
    }

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
            val firstUnit = repository.allUnits.firstOrNull()?.firstOrNull()
            if (firstUnit != null) {
                _selectedUnitId.value = firstUnit.id
            }
        }

        // Listen for Real Inbound Telemetry from ESP32 (BLE or Wi-Fi HTTP)
        viewModelScope.launch {
            esp32Manager.telemetryStream.collect { payload ->
                onReceiveRealTelemetry(payload)
            }
        }

        // Mirror ESP32 manager logs into the serial terminal
        viewModelScope.launch {
            esp32Manager.serialLogs.collect { rawLogs ->
                if (rawLogs.isNotEmpty()) {
                    val last = rawLogs.last()
                    val timeStr = if (last.startsWith("[")) last.substring(1, last.indexOf("]")) else "NOW"
                    val msg = if (last.contains("] ")) last.substringAfter("] ") else last
                    val logType = when {
                        msg.contains("TELEMETRIA", ignoreCase = true) -> "PUB"
                        msg.contains("SYNC", ignoreCase = true) -> "SYS"
                        msg.contains("ERRO", ignoreCase = true) || msg.contains("Falha", ignoreCase = true) -> "WARN"
                        msg.contains("GATT", ignoreCase = true) || msg.contains("HTTP", ignoreCase = true) -> "SYS"
                        else -> "INFO"
                    }
                    _serialLogs.value = (_serialLogs.value.takeLast(30) + SerialLogItem(timeStr, msg, logType))
                }
            }
        }

        // Automatic synchronization listener when ESP32 connection is restored
        viewModelScope.launch {
            var previousStatus = ConnectionStatus.DISCONNECTED
            esp32Manager.connectionState.collect { connState ->
                val isNowConnected = connState.status == ConnectionStatus.CONNECTED_STREAMING
                val wasDisconnected = previousStatus == ConnectionStatus.DISCONNECTED ||
                        previousStatus == ConnectionStatus.CONNECTING ||
                        previousStatus == ConnectionStatus.ERROR

                if (wasDisconnected && isNowConnected) {
                    // Signal re-established! Trigger auto synchronization with Room cache
                    syncOfflineCache(isAuto = true)
                }
                previousStatus = connState.status
            }
        }
    }

    private suspend fun onReceiveRealTelemetry(payload: ESP32TelemetryPayload) {
        val unit = currentUnit.value ?: return
        val primaryDevice = currentDevices.value.firstOrNull() ?: allDevices.value.firstOrNull()
        val devId = primaryDevice?.id ?: 1L

        repository.recordLiveReading(
            unitId = unit.id,
            deviceId = devId,
            temp = payload.temperature,
            humidity = payload.humidity,
            vcc = payload.vcc,
            rssi = payload.rssi
        )

        repository.updateDeviceTelemetry(
            deviceId = devId,
            temp = payload.temperature,
            hum = payload.humidity,
            rssi = payload.rssi,
            vcc = payload.vcc,
            isOnline = true
        )
    }

    // =========================================================================
    //  ESP32 PAIRING & CONNECTION ACTIONS
    // =========================================================================

    fun startBleScan() {
        esp32Manager.startBleScan()
        showToast("Varredura Bluetooth Low Energy iniciada...")
    }

    fun stopBleScan() {
        esp32Manager.stopBleScan()
    }

    fun connectBleESP32(address: String, name: String = "ESP32 TermoSense") {
        esp32Manager.connectBleDevice(address, name)
        showToast("Conectando ao ESP32 via BLE ($name)...")
    }

    fun connectHttpESP32(
        ipAddress: String,
        port: Int = 80,
        name: String = "ESP32 Wi-Fi Node",
        targetUnitId: Long? = null
    ) {
        esp32Manager.testAndConnectHttp(
            ipAddress = ipAddress,
            port = port,
            deviceName = name,
            onSuccess = {
                val unitId = targetUnitId ?: _selectedUnitId.value
                viewModelScope.launch {
                    val primaryDev = currentDevices.value.firstOrNull()
                    if (primaryDev != null) {
                        repository.updateDeviceAddressAndProtocol(primaryDev.id, ipAddress, isOnline = true)
                    }
                }
                showToast("ESP32 conectado com sucesso via Wi-Fi IP ($ipAddress)!")
                _showPairingDialog.value = false
            },
            onError = { err ->
                showToast("Erro ao conectar ESP32: $err")
            }
        )
    }

    fun disconnectESP32() {
        esp32Manager.disconnectCurrent()
        viewModelScope.launch {
            val primaryDev = currentDevices.value.firstOrNull()
            if (primaryDev != null) {
                repository.updateDeviceAddressAndProtocol(primaryDev.id, primaryDev.ipAddress, isOnline = false)
            }
        }
        showToast("Módulo ESP32 desconectado.")
    }

    fun sendESP32RelayCommand(state: Boolean) {
        esp32Manager.sendRelayCommand(state) { success ->
            val unit = currentUnit.value
            if (unit != null) {
                viewModelScope.launch {
                    repository.toggleCooler(unit.id, state)
                }
            }
            showToast(
                if (success) "Comando enviado ao ESP32: Relé ${if (state) "LIGADO" else "DESLIGADO"}"
                else "Falha ao enviar comando de relé ao ESP32"
            )
        }
    }

    fun clearTelemetryHistory() {
        viewModelScope.launch {
            val unit = currentUnit.value
            if (unit != null) {
                repository.clearTelemetryByUnit(unit.id)
                showToast("Histórico do setor ${unit.name} limpo. Aguardando leituras reais do ESP32.")
            } else {
                repository.clearEntireTelemetryHistory()
                showToast("Histórico limpo com sucesso.")
            }
        }
    }

    fun recordManualCalibratedReading(temperature: Double, humidity: Double, calibratorTool: String = "Incoterm / Fluke 1524") {
        val unit = currentUnit.value ?: return
        val primaryDevice = currentDevices.value.firstOrNull() ?: return
        viewModelScope.launch {
            repository.recordLiveReading(
                unitId = unit.id,
                deviceId = primaryDevice.id,
                temp = temperature,
                humidity = humidity,
                vcc = 3.30,
                rssi = -45
            )
            repository.updateDeviceTelemetry(
                deviceId = primaryDevice.id,
                temp = temperature,
                hum = humidity,
                rssi = -45,
                vcc = 3.30,
                isOnline = true
            )
            showToast("Medição oficial de calibração registrada ($calibratorTool): ${temperature}°C | ${humidity}% UR.")
        }
    }

    // =========================================================================
    //  ROOM OFFLINE CACHE & SYNCHRONIZATION CONTROLS
    // =========================================================================

    fun syncOfflineCache(isAuto: Boolean = false) {
        viewModelScope.launch {
            _syncState.value = _syncState.value.copy(isSyncing = true)
            val syncedCount = repository.syncOfflineCacheToMainTelemetry()
            _syncState.value = SyncSummaryState(
                lastSyncCount = syncedCount,
                lastSyncTimestamp = System.currentTimeMillis(),
                isSyncing = false,
                isAutoSyncEnabled = true,
                message = if (syncedCount > 0) "Sincronizado com sucesso ($syncedCount leituras)" else "Nenhum dado pendente no cache"
            )

            if (syncedCount > 0) {
                val prefix = if (isAuto) "🔄 [Auto-Sync]" else "🔄 [Sync Manual]"
                showToast("$prefix $syncedCount leitura(s) do cache Room restauradas no Dashboard!")
                val logItem = SerialLogItem(
                    "SYNC",
                    "$prefix $syncedCount leituras offline recuperadas do cache Room SQLite e sincronizadas.",
                    "SYS"
                )
                _serialLogs.value = (_serialLogs.value.takeLast(30) + logItem)
            } else if (!isAuto) {
                showToast("O cache Room está 100% em dia. Não há leituras offline pendentes.")
            }
        }
    }

    fun simulateOfflineDataCollection(count: Int = 12) {
        val unit = currentUnit.value ?: return
        viewModelScope.launch {
            val generated = repository.simulateOfflineBlackoutData(unit.id, count)
            showToast("📦 $generated leitura(s) geradas e armazenadas no cache local Room (Simulação de Perda de Sinal).")
            val logItem = SerialLogItem(
                "CACHE",
                "Perda de conexão simulada no setor ${unit.code}. $generated registros alocados na tabela offline_telemetry_cache.",
                "WARN"
            )
            _serialLogs.value = (_serialLogs.value.takeLast(30) + logItem)
        }
    }

    fun bufferManualOfflineReading(temperature: Double, humidity: Double) {
        val unit = currentUnit.value ?: return
        val primaryDevice = currentDevices.value.firstOrNull() ?: allDevices.value.firstOrNull()
        val devId = primaryDevice?.id ?: 1L
        viewModelScope.launch {
            repository.bufferOfflineReading(
                unitId = unit.id,
                deviceId = devId,
                temp = temperature,
                hum = humidity,
                vcc = 3.31,
                rssi = 0,
                deviceName = primaryDevice?.name ?: "ESP32 TermoSense Node",
                captureReason = "MANUAL_CONTINGENCY"
            )
            showToast("📦 Leitura offline de contingência (${temperature}°C) salva no cache Room!")
        }
    }

    fun clearOfflineCache() {
        viewModelScope.launch {
            repository.clearOfflineCache()
            showToast("Cache local offline Room limpo com sucesso.")
        }
    }

    // =========================================================================
    //  FIREBASE CLOUD MESSAGING (FCM) CONTROLS & BACKGROUND DISPATCH
    // =========================================================================

    fun testFcmPushBackgroundAlert(
        targetUnitId: Long? = null,
        simulatedTemp: Double = 9.4,
        simulatedHum: Double = 68.0,
        severity: String = "CRITICAL"
    ) {
        val unit = if (targetUnitId != null) {
            allUnits.value.firstOrNull { it.id == targetUnitId } ?: currentUnit.value
        } else currentUnit.value

        val targetUnit = unit ?: allUnits.value.firstOrNull() ?: return
        val dev = currentDevices.value.firstOrNull() ?: allDevices.value.firstOrNull()
        val devId = dev?.id ?: 1L
        val devLabel = dev?.name ?: "ESP32 Sala de Vacinas"

        viewModelScope.launch {
            val title = if (severity == "CRITICAL") "🚨 FCM PUSH: Excursão Térmica (${targetUnit.code})" else "⚠️ FCM PUSH: Alerta Preventivo (${targetUnit.code})"
            val message = "${targetUnit.name} registrou ${String.format("%.1f", simulatedTemp)}°C (Faixa autorizada: ${targetUnit.minTemp}°C a ${targetUnit.maxTemp}°C)."

            // 1. Trigger background high priority notification via NotificationHelper
            notificationHelper.sendThermalAlert(
                title = title,
                message = message,
                isCritical = severity == "CRITICAL",
                unitKey = "fcm_test_${targetUnit.id}",
                notificationId = (4000 + targetUnit.id).toInt()
            )

            // 2. Persist in Room database (as if arrived via FCM service)
            val alert = AlertEventEntity(
                unitId = targetUnit.id,
                deviceId = devId,
                title = title,
                message = message,
                severity = severity,
                currentTemp = simulatedTemp,
                limitTemp = if (simulatedTemp > targetUnit.maxTemp) targetUnit.maxTemp else targetUnit.minTemp,
                timestamp = System.currentTimeMillis(),
                isResolved = false,
                isSilenced = false,
                nodeLabel = devLabel
            )
            repository.insertAlert(alert)

            // 3. Record telemetry reading
            repository.recordLiveReading(
                unitId = targetUnit.id,
                deviceId = devId,
                temp = simulatedTemp,
                humidity = simulatedHum,
                vcc = 3.32,
                rssi = -52
            )

            repository.updateDeviceTelemetry(
                deviceId = devId,
                temp = simulatedTemp,
                hum = simulatedHum,
                rssi = -52,
                vcc = 3.32,
                isOnline = true
            )

            val logItem = SerialLogItem(
                "FCM",
                "Push FCM recebido em background: Excursão de ${simulatedTemp}°C em ${targetUnit.code}.",
                "WARN"
            )
            _serialLogs.value = (_serialLogs.value.takeLast(30) + logItem)
            showToast("🔔 Push FCM disparado com sucesso! Notificação em segundo plano enviada.")
        }
    }

    fun toggleFcmTopic(topic: String, isSubscribed: Boolean) {
        if (isSubscribed) {
            fcmTokenManager.subscribeToTopic(topic) {
                showToast("Inscrito nas notificações FCM do tópico '$topic'.")
            }
        } else {
            fcmTokenManager.unsubscribeFromTopic(topic) {
                showToast("Inscrição removida do tópico '$topic'.")
            }
        }
    }

    fun refreshFcmToken() {
        fcmTokenManager.fetchRegistrationToken { token ->
            if (token != null) {
                showToast("Token FCM atualizado: ${token.take(12)}...")
            }
        }
    }

    // =========================================================================
    //  SELECTION & NAVIGATION
    // =========================================================================

    fun selectUnit(unitId: Long) {
        _selectedUnitId.value = unitId
    }

    fun setTimeframe(filter: TimeframeFilter) {
        _selectedTimeframe.value = filter
    }

    fun setMetric(metric: MetricType) {
        _selectedMetric.value = metric
    }

    fun toggleCooler() {
        val unit = currentUnit.value ?: return
        val newState = !unit.isCoolerActive
        sendESP32RelayCommand(newState)
    }

    fun toggleOfflineMode() {
        val unit = currentUnit.value ?: return
        val newState = !unit.isOfflineMode
        viewModelScope.launch {
            repository.toggleOfflineMode(unit.id, newState)
            showToast(if (newState) "Modo Offline Ativado: Armazenamento no ESP32 Flash/SD" else "Modo Online Restaurado: Sincronia de telemetria ativa")
        }
    }

    fun silenceAlert(alertId: Long) {
        viewModelScope.launch {
            repository.silenceAlert(alertId)
            showToast("Alerta silenciado por 30 minutos.")
        }
    }

    fun resolveAlert(alertId: Long) {
        viewModelScope.launch {
            repository.resolveAlert(alertId)
            showToast("Alerta marcado como resolvido e auditado.")
        }
    }

    fun toggleRule(rule: AutomationRuleEntity) {
        viewModelScope.launch {
            repository.toggleRule(rule)
            showToast(if (!rule.isEnabled) "Automação ARMADA e enviada ao ESP32." else "Regra DESATIVADA.")
        }
    }

    fun updateThresholds(minTemp: Double, maxTemp: Double, minHum: Double, maxHum: Double) {
        val unit = currentUnit.value ?: return
        viewModelScope.launch {
            repository.updateUnitThresholds(unit.id, minTemp, maxTemp, minHum, maxHum)
            _showThresholdDialog.value = false
            showToast("Limiares do setor ${unit.name} salvos: ${minTemp}°C a ${maxTemp}°C (Conforme RDC ANVISA).")
        }
    }

    fun updateSectorThresholds(unitId: Long, minTemp: Double, maxTemp: Double, minHum: Double, maxHum: Double) {
        viewModelScope.launch {
            repository.updateUnitThresholds(unitId, minTemp, maxTemp, minHum, maxHum)
            showToast("Limiares do setor atualizados: ${minTemp}°C a ${maxTemp}°C | ${minHum}% a ${maxHum}% UR.")
        }
    }

    fun addNewSector(
        name: String,
        code: String,
        profileType: String,
        location: String,
        minTemp: Double,
        maxTemp: Double,
        minHum: Double,
        maxHum: Double,
        coolerPin: String
    ) {
        viewModelScope.launch {
            val newSector = HealthUnitEntity(
                name = name,
                code = code,
                profileType = profileType,
                minTemp = minTemp,
                maxTemp = maxTemp,
                minHumidity = minHum,
                maxHumidity = maxHum,
                location = location,
                coolerRelayPin = coolerPin,
                isCoolerActive = false,
                isOfflineMode = false
            )
            val newId = repository.addNewUnit(newSector)
            _selectedUnitId.value = newId
            _showNewSectorDialog.value = false
            showToast("Novo setor '$name' configurado e pronto para monitoramento.")
        }
    }

    fun deleteSector(sector: HealthUnitEntity) {
        viewModelScope.launch {
            repository.deleteUnit(sector)
            showToast("Setor '${sector.name}' removido das configurações.")
        }
    }

    fun addNewDevice(name: String, model: String, sensorType: String, ip: String, mac: String, samplingMs: Long) {
        val unitId = _selectedUnitId.value
        viewModelScope.launch {
            val device = DeviceNodeEntity(
                unitId = unitId,
                name = name,
                model = model,
                sensorType = sensorType,
                ipAddress = ip,
                macAddress = mac,
                samplingRateMs = samplingMs,
                isOnline = false,
                currentTemp = 0.0,
                currentHumidity = 0.0,
                uptimeString = "00d 00h 00m"
            )
            repository.addNewDevice(device)
            _showNewDeviceDialog.value = false
            showToast("Módulo ESP32 '$name' cadastrado com sucesso!")
        }
    }

    fun addNewRule(name: String, condition: String, action: String, targetType: String) {
        val unitId = _selectedUnitId.value
        viewModelScope.launch {
            val rule = AutomationRuleEntity(
                unitId = unitId,
                name = name,
                conditionText = condition,
                actionText = action,
                isEnabled = true,
                targetType = targetType
            )
            repository.addNewRule(rule)
            _showNewRuleDialog.value = false
            showToast("Nova regra '$name' adicionada ao motor de automação.")
        }
    }

    fun adjustSamplingRate(deviceId: Long, deltaMs: Long) {
        val device = currentDevices.value.find { it.id == deviceId } ?: return
        val newRate = (device.samplingRateMs + deltaMs).coerceIn(500L, 60000L)
        viewModelScope.launch {
            repository.updateDeviceSamplingRate(deviceId, newRate)
            showToast("Taxa de amostragem alterada para ${newRate}ms")
        }
    }

    fun generateAndShareReport(
        isPdf: Boolean,
        responsibleTechnician: String,
        registryNumber: String,
        monthPeriod: String,
        observations: String
    ) {
        val unit = currentUnit.value ?: return
        val readings = recentReadings.value
        viewModelScope.launch {
            val temps = readings.map { it.temperature }
            val minT = if (temps.isNotEmpty()) temps.minOrNull() ?: unit.minTemp else unit.minTemp
            val maxT = if (temps.isNotEmpty()) temps.maxOrNull() ?: unit.maxTemp else unit.maxTemp
            val meanT = if (temps.isNotEmpty()) temps.average() else (unit.minTemp + unit.maxTemp) / 2.0
            val hums = readings.map { it.humidity }
            val minH = if (hums.isNotEmpty()) hums.minOrNull() ?: unit.minHumidity else unit.minHumidity
            val maxH = if (hums.isNotEmpty()) hums.maxOrNull() ?: unit.maxHumidity else unit.maxHumidity

            val excursionCount = readings.count { it.isExcursion }
            val compliancePercent = if (readings.isNotEmpty()) {
                ((readings.size - excursionCount).toDouble() / readings.size.toDouble()) * 100.0
            } else 100.0

            val report = ComplianceReportEntity(
                unitId = unit.id,
                unitName = unit.name,
                monthPeriod = monthPeriod,
                responsibleTechnician = responsibleTechnician,
                professionalRegistry = registryNumber,
                minTempRecorded = Math.round(minT * 10.0) / 10.0,
                maxTempRecorded = Math.round(maxT * 10.0) / 10.0,
                meanTempRecorded = Math.round(meanT * 10.0) / 10.0,
                meanKineticTemp = Math.round(meanT * 10.0) / 10.0,
                minHumidityRecorded = Math.round(minH * 10.0) / 10.0,
                maxHumidityRecorded = Math.round(maxH * 10.0) / 10.0,
                totalReadings = readings.size.coerceAtLeast(1),
                excursionMinutes = excursionCount * 2,
                compliancePercentage = Math.round(compliancePercent * 10.0) / 10.0,
                observations = observations
            )

            repository.insertComplianceReport(report)
            _showExportDialog.value = false

            val context = getApplication<Application>().applicationContext
            if (isPdf) {
                val file = ReportExporter.exportToPdf(context, unit, report, readings)
                ReportExporter.shareFile(context, file, "application/pdf", "Laudo de Conformidade Térmica - ${unit.code}")
            } else {
                val file = ReportExporter.exportToCsv(context, unit, readings)
                ReportExporter.shareFile(context, file, "text/csv", "Relatório Térmico Excel/CSV - ${unit.code}")
            }
            showToast("Relatório de conformidade gerado com sucesso!")
        }
    }

    fun openPairingDialog() { _showPairingDialog.value = true }
    fun closePairingDialog() { _showPairingDialog.value = false }
    fun openNewSectorDialog() { _showNewSectorDialog.value = true }
    fun closeNewSectorDialog() { _showNewSectorDialog.value = false }
    fun openThresholdDialog() { _showThresholdDialog.value = true }
    fun closeThresholdDialog() { _showThresholdDialog.value = false }
    fun openNewDeviceDialog() { _showNewDeviceDialog.value = true }
    fun closeNewDeviceDialog() { _showNewDeviceDialog.value = false }
    fun openNewRuleDialog() { _showNewRuleDialog.value = true }
    fun closeNewRuleDialog() { _showNewRuleDialog.value = false }
    fun openExportDialog() { _showExportDialog.value = true }
    fun closeExportDialog() { _showExportDialog.value = false }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        esp32Manager.disconnectCurrent()
    }
}
