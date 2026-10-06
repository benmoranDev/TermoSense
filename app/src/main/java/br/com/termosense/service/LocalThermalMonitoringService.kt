package br.com.termosense.service

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import br.com.termosense.MainActivity
import br.com.termosense.data.repository.TermoSenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LocalThermalMonitoringService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var repository: TermoSenseRepository
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        repository = TermoSenseRepository(applicationContext)
        notificationHelper = NotificationHelper(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startMonitoringLoop()
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "termosense_foreground_monitor"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Monitoramento Contínuo TermoSense",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Garante a vigilância contínua de temperatura e umidade em segundo plano."
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.stat_notify_sync)
            .setContentTitle("TermoSense: Vigilância Ativa")
            .setContentText("Monitorando limites de temperatura e umidade da Rede de Frio.")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(999, notification)
    }

    private fun startMonitoringLoop() {
        serviceScope.launch {
            while (isActive) {
                delay(5000) // check limits periodically
                val units = repository.allUnits.firstOrNull() ?: continue
                for (unit in units) {
                    val devices = repository.getDevicesByUnit(unit.id).firstOrNull() ?: continue
                    for (device in devices) {
                        // Check temperature threshold
                        if (device.currentTemp < unit.minTemp || device.currentTemp > unit.maxTemp) {
                            val isOver = device.currentTemp > unit.maxTemp
                            val title = if (isOver) "🚨 Temperatura Alta: ${unit.code}" else "❄️ Sub-resfriamento: ${unit.code}"
                            val msg = "${unit.name}: ${String.format("%.1f", device.currentTemp)}°C (Limites: ${unit.minTemp}°C a ${unit.maxTemp}°C)."
                            notificationHelper.sendThermalAlert(
                                title = title,
                                message = msg,
                                isCritical = true,
                                unitKey = "unit_${unit.id}_temp",
                                notificationId = (1000 + unit.id).toInt()
                            )
                        }

                        // Check humidity threshold
                        if (device.currentHumidity < unit.minHumidity || device.currentHumidity > unit.maxHumidity) {
                            val isOver = device.currentHumidity > unit.maxHumidity
                            val title = if (isOver) "💧 Umidade Crítica Alta: ${unit.code}" else "⚠️ Umidade Muito Baixa: ${unit.code}"
                            val msg = "${unit.name}: ${String.format("%.1f", device.currentHumidity)}% UR (Limites: ${unit.minHumidity}% a ${unit.maxHumidity}% UR)."
                            notificationHelper.sendHumidityAlert(
                                title = title,
                                message = msg,
                                isCritical = true,
                                unitKey = "unit_${unit.id}_hum",
                                notificationId = (2000 + unit.id).toInt()
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, LocalThermalMonitoringService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LocalThermalMonitoringService::class.java)
            context.stopService(intent)
        }
    }
}
