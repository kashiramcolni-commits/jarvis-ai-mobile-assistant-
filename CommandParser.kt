package com.example.jarvis.commands

import com.example.jarvis.voice.WakeWordManager

class CommandParser(private val wakeWordManager: WakeWordManager) {

    fun parse(rawInput: String): JarvisAction {
        val clean = wakeWordManager.stripWakeWord(rawInput).trim()
        val lower = clean.lowercase()

        if (lower.isBlank()) {
            return JarvisAction.Unknown
        }

        // 1. BATTERY COMMANDS
        // "battery kitni hai", "battery status", "phone charging par hai", "battery percentage", "check battery"
        if (lower.contains("battery") || lower.contains("charging") || lower.contains("charge kitna")) {
            return JarvisAction.BatteryInfo
        }

        // 2. FLASHLIGHT COMMANDS
        // "flashlight on", "torch chalu karo", "torch jalao", "torch on"
        if ((lower.contains("flashlight") || lower.contains("torch")) &&
            (lower.contains("on") || lower.contains("chalu") || lower.contains("jalao") || lower.contains("start"))
        ) {
            return JarvisAction.FlashlightOn
        }
        // "flashlight off", "torch band karo", "torch bujhao", "torch off"
        if ((lower.contains("flashlight") || lower.contains("torch")) &&
            (lower.contains("off") || lower.contains("band") || lower.contains("bujhao") || lower.contains("stop"))
        ) {
            return JarvisAction.FlashlightOff
        }

        // 3. NOTIFICATION COMMANDS
        // "kya notification aaya hai", "latest notification batao", "notification padho", "read notifications"
        if (lower.contains("notification") || lower.contains("message aaya")) {
            return JarvisAction.ReadNotifications
        }

        // 4. DEVICE INFO COMMANDS
        // "phone ka model batao", "android version", "device info", "storage kitni", "storage kitna"
        if (lower.contains("model") || lower.contains("android version") ||
            lower.contains("device info") || lower.contains("storage") ||
            (lower.contains("phone") && lower.contains("info"))
        ) {
            return JarvisAction.DeviceInfo
        }

        // 5. CALL & CONTACT COMMANDS
        if (lower.contains("call log") || lower.contains("call history") || lower.contains("call list")) {
            return JarvisAction.CallHistory
        }

        // "call [name]", "[name] ko call karo", "[name] ko phone lagao"
        val callRegex1 = "^(?:please\\s+)?(?:call|phone|dial)\\s+(.+)".toRegex()
        val callRegex2 = "(.+?)\\s*(?:ko\\s+)?(?:call\\s+karo|phone\\s+lagao|call\\s+lagao|ko\\s+call)".toRegex()

        val callMatch1 = callRegex1.find(lower)
        if (callMatch1 != null) {
            val contact = callMatch1.groupValues[1].trim()
            if (contact.isNotEmpty() && !contact.contains("history") && !contact.contains("log")) {
                return JarvisAction.CallContact(contact)
            }
        }

        val callMatch2 = callRegex2.find(lower)
        if (callMatch2 != null) {
            val contact = callMatch2.groupValues[1].trim()
            if (contact.isNotEmpty() && !contact.contains("history")) {
                return JarvisAction.CallContact(contact)
            }
        }

        // Search contact
        if (lower.contains("contact search") || lower.contains("search contact")) {
            val target = lower.replace("contact search karo", "")
                .replace("search contact", "")
                .replace("contact search", "")
                .trim()
            return JarvisAction.SearchContact(target.ifBlank { "All" })
        }

        // 6. VOLUME COMMANDS
        if (lower.contains("volume") || lower.contains("awaz")) {
            if (lower.contains("badhao") || lower.contains("up") || lower.contains("increase") || lower.contains("tez")) {
                return JarvisAction.VolumeUp
            }
            if (lower.contains("kam") || lower.contains("down") || lower.contains("decrease") || lower.contains("dheemi")) {
                return JarvisAction.VolumeDown
            }
            if (lower.contains("mute") || lower.contains("shant")) {
                return JarvisAction.VolumeMute
            }
        }

        // 7. REMINDERS & ALARMS
        if (lower.contains("reminder") || lower.contains("yaad dilana") || lower.contains("alarm")) {
            val minRegex = "(\\d+)\\s*(?:minute|min|m)".toRegex()
            val match = minRegex.find(lower)
            val mins = match?.groupValues?.get(1)?.toIntOrNull() ?: 10
            return JarvisAction.SetReminder(clean, mins)
        }

        // 8. DATE & TIME
        if (lower.contains("time") || lower.contains("kitne baje") ||
            lower.contains("date") || lower.contains("tarikh")
        ) {
            return JarvisAction.DateTimeQuery
        }

        // 9. CONNECTIVITY SETTINGS
        if (lower.contains("wifi") || lower.contains("wi-fi")) {
            return JarvisAction.ConnectivitySettings("WIFI")
        }
        if (lower.contains("bluetooth")) {
            return JarvisAction.ConnectivitySettings("BLUETOOTH")
        }

        // 10. APP LAUNCH COMMANDS
        // "open instagram", "instagram kholo", "instagram open karo", "youtube chalao"
        val openAppRegex1 = "^(?:please\\s+)?(?:open|launch|start)\\s+(.+)".toRegex()
        val openAppRegex2 = "(.+?)\\s*(?:kholo|open\\s+karo|chalao|lagao|start\\s+karo)".toRegex()

        val appMatch1 = openAppRegex1.find(lower)
        if (appMatch1 != null) {
            val app = appMatch1.groupValues[1].trim()
            return JarvisAction.OpenApp(app)
        }

        val appMatch2 = openAppRegex2.find(lower)
        if (appMatch2 != null) {
            val app = appMatch2.groupValues[1].trim()
            // Make sure it's not a flashlight or calling command
            if (!app.contains("torch") && !app.contains("flashlight") && !app.contains("call")) {
                return JarvisAction.OpenApp(app)
            }
        }

        // Direct app name mentions like "Instagram", "WhatsApp", "YouTube"
        val commonApps = listOf("instagram", "youtube", "whatsapp", "chrome", "camera", "calculator", "maps", "settings", "spotify")
        for (ca in commonApps) {
            if (lower == ca || lower == "$ca please") {
                return JarvisAction.OpenApp(ca)
            }
        }

        // 11. GENERAL AI QUESTION OR CONVERSATION
        // Queries like "Tell me a joke", "Who is Iron Man?", "Explain quantum mechanics", "Aap kaun ho", etc.
        return JarvisAction.GeneralAi(clean)
    }
}
