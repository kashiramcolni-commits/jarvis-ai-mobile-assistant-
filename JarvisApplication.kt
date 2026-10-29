package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class JarvisApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Reminders Channel
            val reminderChannel = NotificationChannel(
                "jarvis_reminders_channel",
                "JARVIS Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts and reminders scheduled by JARVIS"
                enableVibration(true)
            }

            // Foreground Assistant Channel
            val assistantChannel = NotificationChannel(
                "jarvis_assistant_foreground_channel",
                "JARVIS Background Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps JARVIS voice automation active in background"
                setShowBadge(false)
            }

            notificationManager?.createNotificationChannel(reminderChannel)
            notificationManager?.createNotificationChannel(assistantChannel)
        }
    }
}
