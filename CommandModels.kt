package com.example.jarvis.commands

sealed class JarvisAction {
    data class OpenApp(val appName: String) : JarvisAction()
    object BatteryInfo : JarvisAction()
    object FlashlightOn : JarvisAction()
    object FlashlightOff : JarvisAction()
    data class CallContact(val contactName: String) : JarvisAction()
    object CallHistory : JarvisAction()
    data class SearchContact(val query: String) : JarvisAction()
    object ReadNotifications : JarvisAction()
    object VolumeUp : JarvisAction()
    object VolumeDown : JarvisAction()
    object VolumeMute : JarvisAction()
    object DeviceInfo : JarvisAction()
    data class SetReminder(val title: String, val delayMinutes: Int) : JarvisAction()
    object DateTimeQuery : JarvisAction()
    data class ConnectivitySettings(val type: String) : JarvisAction() // "WIFI", "BLUETOOTH", "GENERAL"
    data class GeneralAi(val prompt: String) : JarvisAction()
    object Unknown : JarvisAction()
}

data class CommandExecutionResult(
    val actionName: String,
    val spokenResponse: String,
    val displayText: String,
    val isSuccess: Boolean = true,
    val requiresUiChoice: Boolean = false
)
