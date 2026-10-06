package br.com.termosense.service.esp32

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.ln

class ESP32ConnectionManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "ESP32ConnectionManager"

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _connectionState = MutableStateFlow(ESP32ConnectionState())
    val connectionState: StateFlow<ESP32ConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredESP32>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredESP32>> = _discoveredDevices.asStateFlow()

    private val _telemetryStream = MutableSharedFlow<ESP32TelemetryPayload>(extraBufferCapacity = 64)
    val telemetryStream: SharedFlow<ESP32TelemetryPayload> = _telemetryStream.asSharedFlow()

    private val _serialLogs = MutableStateFlow<List<String>>(emptyList())
    val serialLogs: StateFlow<List<String>> = _serialLogs.asStateFlow()

    private var activeGatt: BluetoothGatt? = null
    private var isScanning = false
    private var scanJob: Job? = null
    private var httpPollJob: Job? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    private val TARGET_SERVICE_UUID = UUID.fromString(ESP32FirmwareTemplate.DEFAULT_BLE_SERVICE_UUID)
    private val TARGET_CHAR_TELEMETRY = UUID.fromString(ESP32FirmwareTemplate.CHARACTERISTIC_TEMP_HUM_UUID)
    private val TARGET_CHAR_RELAY = UUID.fromString(ESP32FirmwareTemplate.CHARACTERISTIC_RELAY_UUID)

    fun log(msg: String) {
        val timeStr = SimpleDateFormat("HH:mm:ss.SS", Locale.getDefault()).format(Date())
        val entry = "[$timeStr] $msg"
        Log.d(tag, entry)
        _serialLogs.value = (_serialLogs.value.takeLast(40) + entry)
    }

    // =========================================================================
    //  BLUETOOTH LOW ENERGY (BLE) SCANNING & CONNECTION
    // =========================================================================

    @SuppressLint("MissingPermission")
    fun startBleScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            log("AVISO: Bluetooth desativado ou indisponível no dispositivo.")
            _connectionState.value = _connectionState.value.copy(
                status = ConnectionStatus.ERROR,
                errorMessage = "Bluetooth desativado. Ative o Bluetooth para escanear ESP32."
            )
            return
        }

        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            log("ERRO: BluetoothLeScanner indisponível.")
            return
        }

        if (isScanning) return
        isScanning = true
        _discoveredDevices.value = emptyList()
        _connectionState.value = _connectionState.value.copy(status = ConnectionStatus.SCANNING)
        log("Iniciando varredura BLE por módulos ESP32...")

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filters = listOf<ScanFilter>() // Open scan to catch ESP32 by name or service

        try {
            scanner.startScan(filters, scanSettings, bleScanCallback)
        } catch (e: Exception) {
            log("Erro ao iniciar Scan BLE: ${e.message}")
            isScanning = false
            _connectionState.value = _connectionState.value.copy(
                status = ConnectionStatus.ERROR,
                errorMessage = "Falha no scanner BLE: ${e.message}"
            )
            return
        }

        // Auto stop scan after 12 seconds
        scanJob?.cancel()
        scanJob = scope.launch {
            delay(12000)
            stopBleScan()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopBleScan() {
        if (!isScanning) return
        isScanning = false
        scanJob?.cancel()
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(bleScanCallback)
            log("Varredura BLE finalizada. ${_discoveredDevices.value.size} dispositivo(s) encontrado(s).")
            if (_connectionState.value.status == ConnectionStatus.SCANNING) {
                _connectionState.value = _connectionState.value.copy(status = ConnectionStatus.DISCONNECTED)
            }
        } catch (e: Exception) {
            Log.e(tag, "Erro ao parar scan BLE", e)
        }
    }

    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val device = result?.device ?: return
            val name = device.name ?: result.scanRecord?.deviceName ?: "ESP32 TermoSense Node"
            val address = device.address ?: return
            val rssi = result.rssi

            // Filter ESP32 or TermoSense devices
            val isEspCandidate = name.contains("ESP", ignoreCase = true) ||
                    name.contains("Termo", ignoreCase = true) ||
                    name.contains("Sensor", ignoreCase = true) ||
                    result.scanRecord?.serviceUuids?.any { it.uuid == TARGET_SERVICE_UUID } == true

            val currentList = _discoveredDevices.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.address == address }

            val discovered = DiscoveredESP32(
                name = name,
                address = address,
                rssi = rssi,
                protocol = ConnectionProtocol.BLE_GATT,
                sensorType = if (name.contains(
                        "DHT",
                        ignoreCase = true
                    )
                ) "DHT22" else "DS18B20 / SHT31",
                model = if (name.contains("S3", ignoreCase = true)) "ESP32-S3" else "ESP32 WROOM",
                isPaired = false,
                lastSeenTimestamp = System.currentTimeMillis()
            )

            if (existingIndex >= 0) {
                currentList[existingIndex] = discovered
            } else {
                currentList.add(discovered)
                log("Encontrado ESP32: '$name' [$address] RSSI: ${rssi}dBm")
            }
            _discoveredDevices.value = currentList
        }

        override fun onScanFailed(errorCode: Int) {
            log("Scan BLE falhou com código: $errorCode")
            isScanning = false
            _connectionState.value = _connectionState.value.copy(
                status = ConnectionStatus.ERROR,
                errorMessage = "Falha no scan BLE ($errorCode)"
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun connectBleDevice(address: String, deviceName: String = "ESP32 TermoSense") {
        stopBleScan()
        disconnectCurrent()

        val bluetoothDevice: BluetoothDevice = try {
            bluetoothAdapter?.getRemoteDevice(address) ?: run {
                log("Dispositivo BLE $address não encontrado no adaptador.")
                return
            }
        } catch (e: Exception) {
            log("Endereço MAC inválido: $address")
            return
        }

        log("Conectando ao ESP32 GATT [$address]...")
        _connectionState.value = ESP32ConnectionState(
            status = ConnectionStatus.CONNECTING,
            connectedDeviceName = deviceName,
            connectedAddress = address,
            protocol = ConnectionProtocol.BLE_GATT
        )

        activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothDevice.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            bluetoothDevice.connectGatt(context, false, gattCallback)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                log("GATT Conectado com sucesso ao ESP32! Descobrindo serviços...")
                gatt?.discoverServices()
                _connectionState.value = _connectionState.value.copy(
                    status = ConnectionStatus.CONNECTING
                )
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                log("GATT Desconectado do ESP32.")
                _connectionState.value = ESP32ConnectionState(
                    status = ConnectionStatus.DISCONNECTED,
                    errorMessage = if (status != BluetoothGatt.GATT_SUCCESS) "Conexão encerrada (status $status)" else null
                )
                try {
                    activeGatt?.close()
                    activeGatt = null
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                log("Serviços GATT descobertos no ESP32.")
                val service = gatt.getService(TARGET_SERVICE_UUID)
                if (service != null) {
                    val telemetryChar = service.getCharacteristic(TARGET_CHAR_TELEMETRY)
                    if (telemetryChar != null) {
                        // Enable notifications
                        gatt.setCharacteristicNotification(telemetryChar, true)
                        val descriptor = telemetryChar.getDescriptor(CCCD_UUID)
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                        log("Subscrição de telemetria ativada com sucesso via BLE!")
                    }
                }

                _connectionState.value = _connectionState.value.copy(
                    status = ConnectionStatus.CONNECTED_STREAMING,
                    lastPacketTimestamp = System.currentTimeMillis()
                )
            } else {
                log("Falha ao descobrir serviços GATT no ESP32: status $status")
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            val dataBytes = characteristic?.value ?: return
            val jsonString = String(dataBytes, Charsets.UTF_8)
            parseAndEmitTelemetry(jsonString, protocol = ConnectionProtocol.BLE_GATT)
        }
    }

    // =========================================================================
    //  WI-FI / HTTP REST & DIRECT IP CONNECTION
    // =========================================================================

    fun testAndConnectHttp(
        ipAddress: String,
        port: Int = 80,
        deviceName: String = "ESP32 Wi-Fi Node",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        disconnectCurrent()
        val cleanIp = ipAddress.trim().removePrefix("http://").removePrefix("https://")
        val url = "http://$cleanIp:$port/telemetry"

        log("Testando handshake HTTP com ESP32 em $url...")
        _connectionState.value = ESP32ConnectionState(
            status = ConnectionStatus.CONNECTING,
            connectedDeviceName = deviceName,
            connectedAddress = "$cleanIp:$port",
            protocol = ConnectionProtocol.WIFI_HTTP_REST
        )

        scope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = httpClient.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && !body.isNullOrEmpty()) {
                    log("Handshake HTTP realizado com sucesso! Resposta: $body")
                    withContext(Dispatchers.Main) {
                        _connectionState.value = ESP32ConnectionState(
                            status = ConnectionStatus.CONNECTED_STREAMING,
                            connectedDeviceName = deviceName,
                            connectedAddress = "$cleanIp:$port",
                            protocol = ConnectionProtocol.WIFI_HTTP_REST,
                            lastPacketTimestamp = System.currentTimeMillis()
                        )
                        startHttpPollingLoop(cleanIp, port)
                        onSuccess()
                    }
                    parseAndEmitTelemetry(body, protocol = ConnectionProtocol.WIFI_HTTP_REST)
                } else {
                    val errMsg = "HTTP status ${response.code}: $body"
                    log("Falha no handshake HTTP: $errMsg")
                    withContext(Dispatchers.Main) {
                        _connectionState.value = ESP32ConnectionState(
                            status = ConnectionStatus.ERROR,
                            errorMessage = "ESP32 não respondeu na porta $port ($errMsg)"
                        )
                        onError(errMsg)
                    }
                }
            } catch (e: Exception) {
                val errMsg = e.message ?: "Timeout ou Host Inalcançável"
                log("Erro de conexão HTTP ao ESP32 [$cleanIp]: $errMsg")
                withContext(Dispatchers.Main) {
                    _connectionState.value = ESP32ConnectionState(
                        status = ConnectionStatus.ERROR,
                        errorMessage = "Falha ao conectar via Wi-Fi: $errMsg"
                    )
                    onError(errMsg)
                }
            }
        }
    }

    private fun startHttpPollingLoop(ip: String, port: Int, intervalMs: Long = 2500) {
        httpPollJob?.cancel()
        httpPollJob = scope.launch(Dispatchers.IO) {
            val url = "http://$ip:$port/telemetry"
            while (isActive) {
                delay(intervalMs)
                try {
                    val request = Request.Builder().url(url).get().build()
                    val response = httpClient.newCall(request).execute()
                    val body = response.body?.string()
                    if (response.isSuccessful && !body.isNullOrEmpty()) {
                        parseAndEmitTelemetry(body, protocol = ConnectionProtocol.WIFI_HTTP_REST)
                    }
                } catch (e: Exception) {
                    log("AVISO: Falha temporária no polling HTTP ESP32: ${e.message}")
                }
            }
        }
    }

    // =========================================================================
    //  TELEMETRY PARSER & DISPATCHER
    // =========================================================================

    private fun parseAndEmitTelemetry(rawPayload: String, protocol: ConnectionProtocol) {
        try {
            val json = JSONObject(rawPayload)
            val temp = when {
                json.has("temperature") -> json.getDouble("temperature")
                json.has("temp") -> json.getDouble("temp")
                json.has("t") -> json.getDouble("t")
                else -> return
            }

            val humidity = when {
                json.has("humidity") -> json.getDouble("humidity")
                json.has("hum") -> json.getDouble("hum")
                json.has("h") -> json.getDouble("h")
                else -> 0.0
            }

            val relay = when {
                json.has("relayState") -> json.getBoolean("relayState")
                json.has("relay") -> json.getBoolean("relay")
                json.has("r") -> (json.optInt("r", 0) == 1)
                else -> false
            }

            val rssi = json.optInt("rssi", -55)
            val vcc = json.optDouble("vcc", 3.32)
            val sensor = json.optString("sensor", "DHT22 / DS18B20")
            val uptime = json.optLong("uptime", 0L)

            val a = 17.27
            val b = 237.7
            val alpha = ((a * temp) / (b + temp)) + ln(humidity.coerceAtLeast(1.0) / 100.0)
            val dewPoint = Math.round(((b * alpha) / (a - alpha)) * 10.0) / 10.0

            val payload = ESP32TelemetryPayload(
                temperature = temp,
                humidity = humidity,
                dewPoint = dewPoint,
                vcc = vcc,
                rssi = rssi,
                sensorModel = sensor,
                isCoolerRelayActive = relay,
                uptimeSeconds = uptime,
                macAddress = _connectionState.value.connectedAddress ?: "",
                ipAddress = _connectionState.value.connectedAddress ?: "",
                timestamp = System.currentTimeMillis()
            )

            _connectionState.value = _connectionState.value.copy(
                status = ConnectionStatus.CONNECTED_STREAMING,
                lastPacketTimestamp = System.currentTimeMillis(),
                currentRssi = rssi,
                currentVcc = vcc,
                isRelayActive = relay
            )

            _telemetryStream.tryEmit(payload)
            log("TELEMETRIA RECEBIDA: ${temp}°C | ${humidity}% UR | Relé: ${if (relay) "LIGADO" else "DESLIGADO"} | RSSI: ${rssi}dBm")
        } catch (e: Exception) {
            log("Erro ao processar JSON de telemetria do ESP32: ${e.message} (Payload: $rawPayload)")
        }
    }

    // =========================================================================
    //  HARDWARE COMMANDS (RELAY / COOLER CONTROL)
    // =========================================================================

    @SuppressLint("MissingPermission")
    fun sendRelayCommand(state: Boolean, onComplete: (Boolean) -> Unit = {}) {
        val currentState = _connectionState.value
        log("Enviando comando de Relé -> ${if (state) "LIGAR" else "DESLIGAR"} via ${currentState.protocol}...")

        if (currentState.protocol == ConnectionProtocol.BLE_GATT) {
            val gatt = activeGatt
            if (gatt != null) {
                val service = gatt.getService(TARGET_SERVICE_UUID)
                val relayChar = service?.getCharacteristic(TARGET_CHAR_RELAY)
                if (relayChar != null) {
                    relayChar.value = if (state) byteArrayOf('1'.code.toByte()) else byteArrayOf('0'.code.toByte())
                    val success = gatt.writeCharacteristic(relayChar)
                    _connectionState.value = _connectionState.value.copy(isRelayActive = state)
                    onComplete(success)
                    return
                }
            }
            onComplete(false)
        } else if (currentState.protocol == ConnectionProtocol.WIFI_HTTP_REST) {
            val address = currentState.connectedAddress ?: return
            val url = "http://$address/relay?state=${if (state) "1" else "0"}"
            scope.launch(Dispatchers.IO) {
                try {
                    val req = Request.Builder().url(url).get().build()
                    val res = httpClient.newCall(req).execute()
                    val ok = res.isSuccessful
                    withContext(Dispatchers.Main) {
                        _connectionState.value = _connectionState.value.copy(isRelayActive = state)
                        onComplete(ok)
                    }
                } catch (e: Exception) {
                    log("Erro ao acionar relé via HTTP: ${e.message}")
                    withContext(Dispatchers.Main) { onComplete(false) }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectCurrent() {
        httpPollJob?.cancel()
        httpPollJob = null
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
        } catch (e: Exception) {
            // Ignore
        }
        activeGatt = null
        _connectionState.value = ESP32ConnectionState(status = ConnectionStatus.DISCONNECTED)
        log("Módulo ESP32 desconectado.")
    }
}
