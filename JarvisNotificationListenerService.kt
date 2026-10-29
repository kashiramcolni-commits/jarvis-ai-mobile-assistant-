package com.example.jarvis.notifications

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class JarvisNotification(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postTime: Long,
    val isOtpOrSensitive: Boolean
)

class JarvisNotificationListenerService : NotificationListenerService() {

    companion object {
        private val _notificationsFlow = MutableStateFlow<List<JarvisNotification>>(emptyList())
        val notificationsFlow: StateFlow<List<JarvisNotification>> = _notificationsFlow.asStateFlow()

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        fun isNotificationAccessGranted(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val myComponent = ComponentName(context, JarvisNotificationListenerService::class.java).flattenToString()
            return enabledListeners.contains(myComponent)
        }

        fun openNotificationAccessSettings(context: Context) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isConnected.value = true
        refreshNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isConnected.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        refreshNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        refreshNotifications()
    }

    private fun refreshNotifications() {
        try {
            val active = activeNotifications ?: return
            val pm = packageManager
            val list = mutableListOf<JarvisNotification>()

            for (sbn in active) {
                // Ignore ongoing or system status bars
                if (sbn.isOngoing) continue

                val extras = sbn.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

                if (title.isBlank() && text.isBlank()) continue

                val appLabel = try {
                    val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    sbn.packageName.substringAfterLast('.')
                }

                val fullText = "$title $text".lowercase()
                val isSensitive = fullText.contains("otp") ||
                        fullText.contains("verification code") ||
                        fullText.contains("security code") ||
                        fullText.contains("do not share") ||
                        fullText.contains("password reset")

                list.add(
                    JarvisNotification(
                        key = sbn.key,
                        packageName = sbn.packageName,
                        appName = appLabel,
                        title = title,
                        text = text,
                        postTime = sbn.postTime,
                        isOtpOrSensitive = isSensitive
                    )
                )
            }

            _notificationsFlow.value = list.sortedByDescending { it.postTime }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
