package br.com.termosense.service.fcm

import android.util.Log
import br.com.termosense.data.model.AlertEventEntity
import br.com.termosense.data.repository.TermoSenseRepository
import br.com.termosense.service.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TermoSenseFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var repository: TermoSenseRepository
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var tokenManager: FCMTokenManager

    override fun onCreate() {
        super.onCreate()
        repository = TermoSenseRepository(applicationContext)
        notificationHelper = NotificationHelper(applicationContext)
        tokenManager = FCMTokenManager(applicationContext)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("TermoSenseFCM", "Novo Token FCM gerado pelo Google Play Services: $token")
        tokenManager.updateToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("TermoSenseFCM", "Mensagem FCM recebida em background! From: ${remoteMessage.from}")

        // 1. Process data payload (standard IoT background delivery)
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            handleDataPayload(data)
            return
        }

        // 2. Process notification payload if present
        remoteMessage.notification?.let { notif ->
            val title = notif.title ?: "🚨 Alerta TermoSense (FCM)"
            val body = notif.body ?: "Excursão de temperatura detectada na unidade de saúde."
            notificationHelper.sendThermalAlert(
                title = title,
                message = body,
                isCritical = true,
                unitKey = "fcm_push_${System.currentTimeMillis()}",
                notificationId = (3000 + (System.currentTimeMillis() % 1000)).toInt()
            )
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val title = data["title"] ?: "🚨 Alerta Térmico em Segundo Plano (FCM)"
        val message = data["message"] ?: data["body"] ?: "Variação crítica de temperatura identificada na Rede de Frio."
        val unitId = data["unitId"]?.toLongOrNull() ?: 1L
        val deviceId = data["deviceId"]?.toLongOrNull() ?: 1L
        val temp = data["temp"]?.toDoubleOrNull() ?: data["temperature"]?.toDoubleOrNull() ?: 8.8
        val hum = data["humidity"]?.toDoubleOrNull() ?: data["hum"]?.toDoubleOrNull() ?: 55.0
        val severity = data["severity"] ?: "CRITICAL"
        val nodeLabel = data["nodeLabel"] ?: "ESP32 Monitor Vacinas"
        val alertType = data["type"] ?: "TEMPERATURE_EXCURSION"

        // 1. Dispatch High Priority Foreground / Heads-up Notification
        val notificationId = (3000 + unitId).toInt()
        if (alertType.contains("HUMIDITY", ignoreCase = true)) {
            notificationHelper.sendHumidityAlert(
                title = title,
                message = message,
                isCritical = severity == "CRITICAL",
                unitKey = "fcm_unit_${unitId}_hum",
                notificationId = notificationId + 500
            )
        } else {
            notificationHelper.sendThermalAlert(
                title = title,
                message = message,
                isCritical = severity == "CRITICAL",
                unitKey = "fcm_unit_${unitId}_temp",
                notificationId = notificationId
            )
        }

        // 2. Persist alert into Room local database asynchronously
        serviceScope.launch {
            try {
                val unit = repository.getUnit(unitId)
                val alertEntity = AlertEventEntity(
                    unitId = unitId,
                    deviceId = deviceId,
                    title = title,
                    message = message,
                    severity = severity,
                    currentTemp = temp,
                    limitTemp = if (temp > (unit?.maxTemp ?: 8.0)) (unit?.maxTemp ?: 8.0) else (unit?.minTemp ?: 2.0),
                    timestamp = System.currentTimeMillis(),
                    isResolved = false,
                    isSilenced = false,
                    nodeLabel = nodeLabel
                )
                repository.insertAlert(alertEntity)

                // Also record the telemetry reading in Room
                repository.recordLiveReading(
                    unitId = unitId,
                    deviceId = deviceId,
                    temp = temp,
                    humidity = hum,
                    vcc = 3.32,
                    rssi = -55
                )

                Log.d("TermoSenseFCM", "Alerta FCM persistido no banco Room com sucesso!")
            } catch (e: Exception) {
                Log.e("TermoSenseFCM", "Erro ao gravar alerta FCM no Room: ${e.message}")
            }
        }
    }
}
