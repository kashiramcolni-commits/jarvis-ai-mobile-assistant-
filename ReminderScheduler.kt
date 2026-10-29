package com.example.jarvis.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

data class ScheduledReminder(
    val id: Int,
    val title: String,
    val triggerTimeMillis: Long
)

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleReminder(title: String, delayMinutes: Int): ScheduledReminder {
        val reminderId = (System.currentTimeMillis() % 100000).toInt()
        val triggerTime = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = "com.example.jarvis.ACTION_TRIGGER_REMINDER"
            putExtra(ReminderBroadcastReceiver.EXTRA_TITLE, title)
            putExtra(ReminderBroadcastReceiver.EXTRA_ID, reminderId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.let { am ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }

        return ScheduledReminder(reminderId, title, triggerTime)
    }

    fun parseReminderFromText(text: String): Pair<String, Int> {
        val lower = text.lowercase()
        // Check for minutes
        val minuteRegex = "(\\d+)\\s*(?:minute|min|m)".toRegex()
        val match = minuteRegex.find(lower)
        val minutes = match?.groupValues?.get(1)?.toIntOrNull() ?: 10

        // Extract title
        var title = text
        val keywordsToRemove = listOf(
            "remind me to", "remind me", "reminder dena", "yaad dilana",
            "reminder lagao", "baad", "after", "in $minutes minutes", "$minutes minute baad"
        )
        for (kw in keywordsToRemove) {
            title = title.replace(kw, "", ignoreCase = true)
        }
        title = title.trim()
        if (title.isBlank()) {
            title = "Follow up task"
        }

        return Pair(title, minutes)
    }
}
