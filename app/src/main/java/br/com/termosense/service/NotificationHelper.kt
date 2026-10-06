package br.com.termosense.service

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import br.com.termosense.MainActivity

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    // Alert cooldown registry to prevent spamming notifications
    private val lastNotificationTimestamps = mutableMapOf<String, Long>()
    private val cooldownMs = 15000L // 15 seconds per unit/type alert cooldown

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val thermalChannel = NotificationChannel(
                CHANNEL_CRITICAL_ALERTS,
                "Alertas Térmicos - Cadeia de Frio",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações urgentes de excursão de temperatura e sub-resfriamento em UBS/AMA"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(soundUri, audioAttributes)
            }

            val humidityChannel = NotificationChannel(
                CHANNEL_HUMIDITY_ALERTS,
                "Alertas de Umidade & Condensação",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de umidade fora da faixa ideal de conservação de medicamentos"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setSound(soundUri, audioAttributes)
            }

            val infoChannel = NotificationChannel(
                CHANNEL_STATUS_UPDATES,
                "Status e Diagnósticos IoT",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Atualizações periódicas de nós ESP32 e conexões MQTT"
            }

            notificationManager.createNotificationChannel(thermalChannel)
            notificationManager.createNotificationChannel(humidityChannel)
            notificationManager.createNotificationChannel(infoChannel)
        }
    }

    fun sendThermalAlert(
        title: String,
        message: String,
        isCritical: Boolean = true,
        unitKey: String = "default_thermal",
        notificationId: Int = 1001
    ) {
        val now = System.currentTimeMillis()
        val lastTime = lastNotificationTimestamps[unitKey] ?: 0L
        if (now - lastTime < cooldownMs) {
            return // Skip within cooldown
        }
        lastNotificationTimestamps[unitKey] = now

        dispatchNotification(
            channelId = CHANNEL_CRITICAL_ALERTS,
            title = title,
            message = message,
            isCritical = isCritical,
            notificationId = notificationId,
            subText = "Cadeia de Frio"
        )
    }

    fun sendHumidityAlert(
        title: String,
        message: String,
        isCritical: Boolean = true,
        unitKey: String = "default_humidity",
        notificationId: Int = 1002
    ) {
        val now = System.currentTimeMillis()
        val lastTime = lastNotificationTimestamps[unitKey] ?: 0L
        if (now - lastTime < cooldownMs) {
            return
        }
        lastNotificationTimestamps[unitKey] = now

        dispatchNotification(
            channelId = CHANNEL_HUMIDITY_ALERTS,
            title = title,
            message = message,
            isCritical = isCritical,
            notificationId = notificationId,
            subText = "Controle de Umidade"
        )
    }

    private fun dispatchNotification(
        channelId: String,
        title: String,
        message: String,
        isCritical: Boolean,
        notificationId: Int,
        subText: String
    ) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_NAVIGATE_TO", "alertas")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText(subText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_menu_view,
                "Ver Gráficos & Alertas",
                pendingIntent
            )
            .setAutoCancel(true)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Handled if notification permission is not yet granted
        }

        // Trigger tactile haptic vibration
        try {
            if (isCritical && vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 400, 150, 400),
                            -1
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 400, 150, 400), -1)
                }
            }
        } catch (_: Exception) {}
    }

    companion object {
        const val CHANNEL_CRITICAL_ALERTS = "termosense_critical_alerts"
        const val CHANNEL_HUMIDITY_ALERTS = "termosense_humidity_alerts"
        const val CHANNEL_STATUS_UPDATES = "termosense_status_updates"
    }
}
