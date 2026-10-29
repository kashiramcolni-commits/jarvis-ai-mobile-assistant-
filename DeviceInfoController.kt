package com.example.jarvis.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.text.DecimalFormat

data class DeviceTelemetry(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val freeStorageGb: Double,
    val totalStorageGb: Double,
    val networkType: String,
    val isOnline: Boolean
)

class DeviceInfoController(private val context: Context) {

    fun getDeviceTelemetry(): DeviceTelemetry {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalGb = (totalBlocks * blockSize).toDouble() / (1024 * 1024 * 1024)
        val freeGb = (availableBlocks * blockSize).toDouble() / (1024 * 1024 * 1024)

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNetwork)

        val isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val netType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi (High Speed)"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular Mobile Data"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            isOnline -> "Connected"
            else -> "Offline / No Connection"
        }

        return DeviceTelemetry(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            freeStorageGb = freeGb,
            totalStorageGb = totalGb,
            networkType = netType,
            isOnline = isOnline
        )
    }

    fun getSpokenDeviceInfo(isHindi: Boolean): String {
        val t = getDeviceTelemetry()
        val df = DecimalFormat("#.#")
        return if (isHindi) {
            "Sir, aapka phone ${t.manufacturer} ${t.model} hai, running Android ${t.androidVersion}. Storage mein lagbhag ${df.format(t.freeStorageGb)} GB khali hai out of ${df.format(t.totalStorageGb)} GB. Network status: ${t.networkType}."
        } else {
            "Sir, your device is a ${t.manufacturer} ${t.model} running Android ${t.androidVersion}. You have ${df.format(t.freeStorageGb)} GB free out of ${df.format(t.totalStorageGb)} GB total storage. Network connection is ${t.networkType}."
        }
    }
}
