package com.example.jarvis.commands

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.jarvis.ai.AiAssistantRepository
import com.example.jarvis.apps.AppLauncher
import com.example.jarvis.contacts.CallController
import com.example.jarvis.contacts.CallInitiationResult
import com.example.jarvis.contacts.ContactsRepository
import com.example.jarvis.device.BatteryController
import com.example.jarvis.device.DeviceInfoController
import com.example.jarvis.device.FlashlightController
import com.example.jarvis.device.VolumeController
import com.example.jarvis.notifications.JarvisNotificationListenerService
import com.example.jarvis.reminders.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommandRouter(
    private val context: Context,
    private val appLauncher: AppLauncher,
    private val batteryController: BatteryController,
    private val flashlightController: FlashlightController,
    private val volumeController: VolumeController,
    private val deviceInfoController: DeviceInfoController,
    private val contactsRepository: ContactsRepository,
    private val callController: CallController,
    private val reminderScheduler: ReminderScheduler,
    private val aiAssistantRepository: AiAssistantRepository
) {

    suspend fun executeAction(
        action: JarvisAction,
        isHindiPreferred: Boolean = false
    ): CommandExecutionResult {
        return when (action) {
            is JarvisAction.OpenApp -> {
                val result = appLauncher.launchAppByName(action.appName)
                CommandExecutionResult(
                    actionName = "OPEN_APP",
                    spokenResponse = result.message,
                    displayText = result.message,
                    isSuccess = result.success
                )
            }

            is JarvisAction.BatteryInfo -> {
                val spoken = batteryController.getSpokenStatus(isHindiPreferred)
                val info = batteryController.getBatteryInfo()
                val display = "⚡ Battery: ${info.percentage}% | Charging: ${if (info.isCharging) "Yes (${info.chargePlug})" else "No"} | Health: ${info.health} | Temp: ${info.temperatureCelsius}°C"
                CommandExecutionResult(
                    actionName = "BATTERY_INFO",
                    spokenResponse = spoken,
                    displayText = display
                )
            }

            is JarvisAction.FlashlightOn -> {
                val result = flashlightController.setFlashlight(true)
                if (result.isSuccess) {
                    val msg = if (isHindiPreferred) "Torch on kar di gayi hai, Sir." else "Flashlight turned on, Sir."
                    CommandExecutionResult("FLASHLIGHT_ON", msg, msg)
                } else {
                    val err = "Flashlight error: ${result.exceptionOrNull()?.message}"
                    CommandExecutionResult("FLASHLIGHT_ON", err, err, isSuccess = false)
                }
            }

            is JarvisAction.FlashlightOff -> {
                val result = flashlightController.setFlashlight(false)
                if (result.isSuccess) {
                    val msg = if (isHindiPreferred) "Torch band kar di gayi hai, Sir." else "Flashlight turned off, Sir."
                    CommandExecutionResult("FLASHLIGHT_OFF", msg, msg)
                } else {
                    val err = "Flashlight error: ${result.exceptionOrNull()?.message}"
                    CommandExecutionResult("FLASHLIGHT_OFF", err, err, isSuccess = false)
                }
            }

            is JarvisAction.CallContact -> {
                when (val callResult = callController.initiateCall(action.contactName)) {
                    is CallInitiationResult.DialOpened -> {
                        val msg = if (isHindiPreferred) {
                            "${callResult.contactName} ke liye dialer open kar diya hai (${callResult.number})."
                        } else {
                            "Opening phone dialer for ${callResult.contactName} (${callResult.number})."
                        }
                        CommandExecutionResult("CALL_CONTACT", msg, msg)
                    }
                    is CallInitiationResult.DirectCalling -> {
                        val msg = "Calling ${callResult.contactName}..."
                        CommandExecutionResult("CALL_CONTACT", msg, msg)
                    }
                    is CallInitiationResult.MultipleFound -> {
                        val names = callResult.contacts.take(3).joinToString { "${it.name} (${it.phoneNumber})" }
                        val msg = "Multiple contacts found: $names. Please specify which contact to call, Sir."
                        CommandExecutionResult("CALL_CONTACT", msg, msg, requiresUiChoice = true)
                    }
                    is CallInitiationResult.NotFound -> {
                        val msg = "No contact found matching '${callResult.name}', Sir."
                        CommandExecutionResult("CALL_CONTACT", msg, msg, isSuccess = false)
                    }
                    is CallInitiationResult.PermissionRequired -> {
                        CommandExecutionResult("CALL_CONTACT", callResult.message, callResult.message, isSuccess = false)
                    }
                }
            }

            is JarvisAction.CallHistory -> {
                val msg = callController.openCallHistory()
                CommandExecutionResult("CALL_HISTORY", msg, msg)
            }

            is JarvisAction.SearchContact -> {
                val contacts = contactsRepository.searchContacts(action.query)
                if (contacts.isEmpty()) {
                    val msg = "No contacts found matching '${action.query}'."
                    CommandExecutionResult("SEARCH_CONTACT", msg, msg)
                } else {
                    val summary = contacts.take(5).joinToString("\n") { "• ${it.name}: ${it.phoneNumber}" }
                    val spoken = "Found ${contacts.size} contact${if (contacts.size > 1) "s" else ""}: ${contacts.first().name} and others."
                    CommandExecutionResult("SEARCH_CONTACT", spoken, "Contacts Found:\n$summary")
                }
            }

            is JarvisAction.ReadNotifications -> {
                if (!JarvisNotificationListenerService.isNotificationAccessGranted(context)) {
                    val msg = "Notification access permission is not enabled. Please enable JARVIS in notification access settings, Sir."
                    JarvisNotificationListenerService.openNotificationAccessSettings(context)
                    CommandExecutionResult("READ_NOTIFICATIONS", msg, msg, isSuccess = false)
                } else {
                    val notifications = JarvisNotificationListenerService.notificationsFlow.value
                    if (notifications.isEmpty()) {
                        val msg = if (isHindiPreferred) {
                            "Sir, filhal koi naya notification nahi aaya hai."
                        } else {
                            "Sir, there are no active status bar notifications."
                        }
                        CommandExecutionResult("READ_NOTIFICATIONS", msg, msg)
                    } else {
                        val safeNotifications = notifications.filter { !it.isOtpOrSensitive }
                        val summaryList = safeNotifications.take(4).map {
                            "From ${it.appName}: ${it.title} - ${it.text}"
                        }
                        val spoken = "You have ${safeNotifications.size} notification${if (safeNotifications.size > 1) "s" else ""}. Latest from ${safeNotifications.first().appName}: ${safeNotifications.first().title}."
                        val display = safeNotifications.take(5).joinToString("\n\n") {
                            "🔔 [${it.appName}] ${it.title}\n${it.text}"
                        }
                        CommandExecutionResult("READ_NOTIFICATIONS", spoken, display)
                    }
                }
            }

            is JarvisAction.VolumeUp -> {
                val msg = volumeController.adjustVolume(true)
                CommandExecutionResult("VOLUME_ADJUST", msg, msg)
            }

            is JarvisAction.VolumeDown -> {
                val msg = volumeController.adjustVolume(false)
                CommandExecutionResult("VOLUME_ADJUST", msg, msg)
            }

            is JarvisAction.VolumeMute -> {
                val msg = volumeController.mute()
                CommandExecutionResult("VOLUME_MUTE", msg, msg)
            }

            is JarvisAction.DeviceInfo -> {
                val spoken = deviceInfoController.getSpokenDeviceInfo(isHindiPreferred)
                val t = deviceInfoController.getDeviceTelemetry()
                val display = "📱 Device: ${t.manufacturer} ${t.model}\n" +
                        "🤖 Android: ${t.androidVersion} (API ${t.apiLevel})\n" +
                        "💾 Storage: ${String.format("%.1f", t.freeStorageGb)} GB Free / ${String.format("%.1f", t.totalStorageGb)} GB Total\n" +
                        "📶 Network: ${t.networkType}"
                CommandExecutionResult("DEVICE_INFO", spoken, display)
            }

            is JarvisAction.SetReminder -> {
                val reminder = reminderScheduler.scheduleReminder(action.title, action.delayMinutes)
                val msg = if (isHindiPreferred) {
                    "Sir, maine ${action.delayMinutes} minute baad ka reminder set kar diya hai: '${reminder.title}'."
                } else {
                    "Reminder set for ${action.delayMinutes} minutes from now: '${reminder.title}', Sir."
                }
                CommandExecutionResult("SET_REMINDER", msg, "⏰ Scheduled: ${reminder.title} in ${action.delayMinutes} mins")
            }

            is JarvisAction.DateTimeQuery -> {
                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                val dateStr = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
                val spoken = if (isHindiPreferred) {
                    "Sir, abhi samay $timeStr ho raha hai, aur aaj $dateStr hai."
                } else {
                    "Sir, the time is $timeStr and today is $dateStr."
                }
                CommandExecutionResult("DATE_TIME", spoken, "🕒 Time: $timeStr\n📅 Date: $dateStr")
            }

            is JarvisAction.ConnectivitySettings -> {
                val intent = when (action.type) {
                    "WIFI" -> Intent(Settings.ACTION_WIFI_SETTINGS)
                    "BLUETOOTH" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    else -> Intent(Settings.ACTION_WIRELESS_SETTINGS)
                }.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(intent)
                val msg = "Opening ${action.type.lowercase().replaceFirstChar { it.uppercase() }} settings, Sir."
                CommandExecutionResult("CONNECTIVITY_SETTINGS", msg, msg)
            }

            is JarvisAction.GeneralAi -> {
                val response = aiAssistantRepository.getAiResponse(action.prompt)
                CommandExecutionResult(
                    actionName = "GENERAL_AI",
                    spokenResponse = response,
                    displayText = response
                )
            }

            is JarvisAction.Unknown -> {
                val msg = if (isHindiPreferred) {
                    "Kripya dobara kahein, Sir. Main aapki command nahi samajh paya."
                } else {
                    "Could you please repeat that, Sir? I did not catch your directive."
                }
                CommandExecutionResult("UNKNOWN", msg, msg, isSuccess = false)
            }
        }
    }
}
