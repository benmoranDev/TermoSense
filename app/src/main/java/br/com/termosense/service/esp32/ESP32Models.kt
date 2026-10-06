package br.com.termosense.service.esp32

enum class ConnectionProtocol {
    BLE_GATT,
    WIFI_HTTP_REST,
    WIFI_WEBSOCKET,
    MANUAL_CALIBRATED
}

enum class ConnectionStatus {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED_STREAMING,
    ERROR
}

data class DiscoveredESP32(
    val name: String,
    val address: String, // MAC or IP
    val rssi: Int,
    val protocol: ConnectionProtocol,
    val sensorType: String = "DHT22 / DS18B20",
    val model: String = "ESP32-S3 / WROOM",
    val isPaired: Boolean = false,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

data class ESP32TelemetryPayload(
    val temperature: Double,
    val humidity: Double,
    val dewPoint: Double = 0.0,
    val vcc: Double = 3.3,
    val rssi: Int = -60,
    val sensorModel: String = "DHT22/DS18B20",
    val isCoolerRelayActive: Boolean = false,
    val uptimeSeconds: Long = 0,
    val macAddress: String = "",
    val ipAddress: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ESP32ConnectionState(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectedDeviceName: String? = null,
    val connectedAddress: String? = null,
    val protocol: ConnectionProtocol = ConnectionProtocol.BLE_GATT,
    val lastPacketTimestamp: Long? = null,
    val currentRssi: Int = 0,
    val currentVcc: Double = 3.3,
    val isRelayActive: Boolean = false,
    val errorMessage: String? = null
)
