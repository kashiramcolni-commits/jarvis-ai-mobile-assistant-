package com.example.jarvis.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class ReminderBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "jarvis_reminders_channel"
        const val EXTRA_TITLE = "extra_reminder_title"
        const val EXTRA_ID = "extra_reminder_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Reminder from JARVIS"
        val reminderId = intent.getIntExtra(EXTRA_ID, 1001)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JARVIS Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts and reminders scheduled by JARVIS"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("JARVIS Protocol Reminder")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Sir, you requested a reminder: $title"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(reminderId, notification)
    }
}
