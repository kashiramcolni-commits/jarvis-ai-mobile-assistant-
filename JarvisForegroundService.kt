package com.example.jarvis.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.jarvis.device.FlashlightController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JarvisForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "jarvis_assistant_foreground_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "com.example.jarvis.action.START_SERVICE"
        const val ACTION_STOP = "com.example.jarvis.action.STOP_SERVICE"
        const val ACTION_TOGGLE_TORCH = "com.example.jarvis.action.TOGGLE_TORCH"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, JarvisForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, JarvisForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var flashlightController: FlashlightController? = null

    override fun onCreate() {
        super.onCreate()
        flashlightController = FlashlightController(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(true)
                stopSelf()
                _isRunning.value = false
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_TORCH -> {
                flashlightController?.toggleFlashlight()
            }
            ACTION_START -> {
                startForegroundWithNotification()
                _isRunning.value = true
            }
            else -> {
                startForegroundWithNotification()
                _isRunning.value = true
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildForegroundNotification()

        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                foregroundServiceType
            )
        } catch (e: Exception) {
            // Fallback for permissions if needed
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_VOICE_TRIGGER", true)
        }
        val pendingTap = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Action
        val stopIntent = Intent(this, JarvisForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Toggle Torch Action
        val torchIntent = Intent(this, JarvisForegroundService::class.java).apply {
            action = ACTION_TOGGLE_TORCH
        }
        val pendingTorch = PendingIntent.getService(
            this,
            2,
            torchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("JARVIS AI • Standby Protocol Active")
            .setContentText("Listening for 'Hey Jarvis' or tap to speak. Systems online.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("JARVIS Core is monitoring commands in background. Tap notification to command or use quick actions below.")
            )
            .setContentIntent(pendingTap)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_launcher_foreground, "🔦 Toggle Torch", pendingTorch)
            .addAction(R.drawable.ic_launcher_foreground, "⏹️ Disengage", pendingStop)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JARVIS Background Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps JARVIS voice automation active in background"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isRunning.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
