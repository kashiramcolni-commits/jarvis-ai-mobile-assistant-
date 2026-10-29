package com.example.jarvis.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean,
    val chargePlug: String, // "AC", "USB", "Wireless", "Battery"
    val health: String,
    val temperatureCelsius: Float,
    val voltageMv: Int
)

class BatteryController(private val context: Context) {

    fun getBatteryInfo(): BatteryInfo {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, iFilter)

        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val calculatedPercentage = if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0
        }

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlugInt: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargePlug = when (chargePlugInt) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Charger"
            else -> "Battery Power"
        }

        val healthInt: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val health = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Normal"
        }

        val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = tempTenths / 10.0f

        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

        return BatteryInfo(
            percentage = calculatedPercentage.coerceIn(0, 100),
            isCharging = isCharging,
            chargePlug = chargePlug,
            health = health,
            temperatureCelsius = tempCelsius,
            voltageMv = voltage
        )
    }

    fun getSpokenStatus(isHindi: Boolean): String {
        val info = getBatteryInfo()
        return if (isHindi) {
            if (info.isCharging) {
                "Sir, aapke phone ki battery ${info.percentage} percent hai aur phone charging par laga hai (${info.chargePlug})."
            } else {
                "Sir, aapke phone ki battery ${info.percentage} percent hai."
            }
        } else {
            if (info.isCharging) {
                "Sir, your battery is at ${info.percentage} percent and currently charging via ${info.chargePlug}."
            } else {
                "Sir, your battery level is at ${info.percentage} percent."
            }
        }
    }
}
