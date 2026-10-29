package com.example.jarvis.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings

data class AppLaunchResult(
    val success: Boolean,
    val appName: String,
    val message: String
)

class AppLauncher(private val context: Context) {

    private val packageMap = mapOf(
        "instagram" to "com.instagram.android",
        "insta" to "com.instagram.android",
        "youtube" to "com.google.android.youtube",
        "yt" to "com.google.android.youtube",
        "whatsapp" to "com.whatsapp",
        "chrome" to "com.android.chrome",
        "google" to "com.google.android.googlequicksearchbox",
        "maps" to "com.google.android.apps.maps",
        "google maps" to "com.google.android.apps.maps",
        "calculator" to "com.google.android.calculator",
        "calc" to "com.google.android.calculator",
        "clock" to "com.google.android.deskclock",
        "alarm" to "com.google.android.deskclock",
        "spotify" to "com.spotify.music",
        "gmail" to "com.google.android.gm",
        "play store" to "com.android.vending",
        "playstore" to "com.android.vending",
        "telegram" to "org.telegram.messenger",
        "twitter" to "com.twitter.android",
        "x" to "com.twitter.android",
        "facebook" to "com.facebook.katana"
    )

    fun launchAppByName(rawName: String): AppLaunchResult {
        val cleanName = rawName.lowercase().trim()

        // Special system handlers
        when {
            cleanName.contains("camera") || cleanName.contains("photo") -> {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                return tryLaunchIntent(intent, "Camera")
            }
            cleanName == "settings" || cleanName.contains("setting") -> {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                return tryLaunchIntent(intent, "Settings")
            }
            cleanName.contains("wifi") -> {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                return tryLaunchIntent(intent, "Wi-Fi Settings")
            }
            cleanName.contains("bluetooth") -> {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                return tryLaunchIntent(intent, "Bluetooth Settings")
            }
        }

        // Check explicit package mappings
        val matchedPackage = packageMap.entries.firstOrNull { cleanName.contains(it.key) }?.value
        if (matchedPackage != null) {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(matchedPackage)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                return AppLaunchResult(true, rawName, "Opening $rawName.")
            }
        }

        // Generic lookup in installed launcher applications
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        for (info in resolveInfos) {
            val appLabel = info.loadLabel(pm).toString().lowercase()
            if (appLabel.contains(cleanName) || cleanName.contains(appLabel)) {
                val packageName = info.activityInfo.packageName
                val launchIntent = pm.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                    return AppLaunchResult(true, appLabel, "Opening ${info.loadLabel(pm)}.")
                }
            }
        }

        // If not installed, offer truthful message and option to get on Google Play
        return AppLaunchResult(
            success = false,
            appName = rawName,
            message = "Application '$rawName' is not installed on this device."
        )
    }

    private fun tryLaunchIntent(intent: Intent, appName: String): AppLaunchResult {
        return try {
            context.startActivity(intent)
            AppLaunchResult(true, appName, "Opening $appName.")
        } catch (e: Exception) {
            AppLaunchResult(false, appName, "Unable to launch $appName: ${e.message}")
        }
    }
}
