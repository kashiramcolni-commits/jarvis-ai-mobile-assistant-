package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.jarvis.commands.CommandParser
import com.example.jarvis.commands.JarvisAction
import com.example.jarvis.device.BatteryController
import com.example.jarvis.voice.WakeWordManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JARVIS AI", appName)
    }

    @Test
    fun testWakeWordDetectionAndStripping() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val wakeWordManager = WakeWordManager(context)

        assertTrue(wakeWordManager.isWakeWordContained("Hey Jarvis, open Instagram"))
        val stripped = wakeWordManager.stripWakeWord("Hey Jarvis, battery kitni hai?")
        assertEquals("battery kitni hai?", stripped)
    }

    @Test
    fun testMultilingualCommandParsing() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val wakeWordManager = WakeWordManager(context)
        val parser = CommandParser(wakeWordManager)

        // 1. Battery Hindi
        val action1 = parser.parse("Jarvis, battery kitni hai?")
        assertTrue(action1 is JarvisAction.BatteryInfo)

        // 2. Flashlight Hindi
        val action2 = parser.parse("Jarvis, torch chalu karo")
        assertTrue(action2 is JarvisAction.FlashlightOn)

        // 3. Flashlight Off
        val action3 = parser.parse("Jarvis, flashlight off")
        assertTrue(action3 is JarvisAction.FlashlightOff)

        // 4. App Launch
        val action4 = parser.parse("Hey Jarvis, open Instagram")
        assertTrue(action4 is JarvisAction.OpenApp && (action4 as JarvisAction.OpenApp).appName == "instagram")

        // 5. Calling Hindi
        val action5 = parser.parse("Jarvis, Mummy ko call karo")
        assertTrue(action5 is JarvisAction.CallContact && (action5 as JarvisAction.CallContact).contactName == "mummy")

        // 6. Notifications
        val action6 = parser.parse("Jarvis, latest notification batao")
        assertTrue(action6 is JarvisAction.ReadNotifications)
    }

    @Test
    fun testBatteryControllerQueriesRealSystem() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val batteryController = BatteryController(context)
        val info = batteryController.getBatteryInfo()
        assertTrue(info.percentage in 0..100)
    }
}
