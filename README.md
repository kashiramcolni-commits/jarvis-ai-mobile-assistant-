# JARVIS AI — Advanced Native Android Personal AI Assistant

An intelligent, futuristic personal AI assistant inspired by **Tony Stark's J.A.R.V.I.S.** and powered by **Google Gemini** conversational intelligence, built natively in **Kotlin** and **Jetpack Compose**.

---

## ⚡ Key Highlights & Features

- **Iron Man Arc Reactor UI**:
  - Custom Canvas animated multi-ring Arc Reactor with stator teeth and plasma core.
  - Rotates, pulses, and dynamically reacts in real-time to speech amplitude (`rmsDb`) and voice states (`IDLE`, `LISTENING`, `PROCESSING`, `SPEAKING`).
  - Dark holographic sci-fi aesthetic with electric cyan, cobalt azure, and amber accents.

- **Multilingual Speech Recognition & Synthesis**:
  - Android native `SpeechRecognizer` with live audio waveform reactivity.
  - Native `TextToSpeech` with auto-switching for **English**, **Hindi (हिंदी)**, and **Hinglish**.
  - Configurable speech rate (0.6x – 1.5x) and pitch.

- **Real Hardware & Device Automation**:
  - **⚡ Battery Telemetry**: Real-time percentage, charging status (AC/USB/Wireless), health, temperature, and voltage via `BatteryManager`.
  - **🔦 Flashlight (Torch)**: Direct `CameraManager` torch mode toggle with real hardware state callbacks.
  - **📱 App Launcher**: Opens Instagram, YouTube, WhatsApp, Chrome, Camera, Maps, Settings, Calculator, Clock, Spotify, and installed apps.
  - **📞 Contacts & Calling**: Safe dialer flow (`ACTION_DIAL`) and direct calling (`ACTION_CALL`) with name matching.
  - **🔔 Notification Listener**: Live status bar notification reader via `NotificationListenerService` with built-in privacy filtering to guard OTPs and 2FA codes.
  - **⏰ Reminders & Alarms**: Scheduled task alerts via Android `AlarmManager` and high-priority notification channels.
  - **🔊 Sound & Connectivity**: Adjust media volume, mute, open Wi-Fi / Bluetooth system menus.
  - **ℹ️ Hardware Telemetry**: Model, manufacturer, Android OS version, and live free/total storage calculations via `StatFs`.

- **Background Assistant Mode**:
  - Persistent Android Foreground Service with quick notification shortcuts ("🔦 Toggle Torch", "🎙️ Listen", "⚡ Battery", "⏹️ Disengage").

- **Local Persistence & Privacy**:
  - Full local chat history stored using **Room Database**.
  - Preferences saved using Jetpack **DataStore**.
  - Local-first intent routing before routing general queries to Gemini.

---

## 🛠️ Technology Stack

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: Clean MVVM (Model-View-ViewModel) + Repository Pattern
- **Persistence**: Room Database + Jetpack DataStore Preferences
- **Asynchronous**: Kotlin Coroutines & StateFlow / SharedFlow
- **Voice**: Android `SpeechRecognizer` & `TextToSpeech`
- **System Services**: `BatteryManager`, `CameraManager`, `AudioManager`, `AlarmManager`, `NotificationListenerService`
- **Testing**: Robolectric JVM local tests

---

## 🚀 Beginner's Guide: How to Build & Run in Android Studio

### 1. Requirements
- Android Studio (Ladybug / Koala or newer recommended)
- JDK 17 or JDK 21
- Android device or emulator running Android 8.0 (API 26) or higher

### 2. Opening the Project
1. Launch **Android Studio**.
2. Select **File > Open...** and select this project directory.
3. Allow Gradle to sync dependencies automatically.

### 3. Generating the APK
- **Debug APK (Quick Test)**:
  1. In Android Studio, click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
  2. The generated APK will be located at:
     `app/build/outputs/apk/debug/app-debug.apk`
- **Via Terminal / Command Line**:
  ```bash
  gradle :app:assembleDebug
  ```

---

## 📋 Physical Device Testing Checklist

When testing on a real Android smartphone:

| Feature | Action / Command | Expected Behavior |
|---|---|---|
| **Microphone Permission** | Tap Mic FAB or Settings > Grant | System permission dialog appears; listening starts |
| **Battery Status** | Say: *"Battery kitni hai?"* | JARVIS speaks actual phone battery percentage and charging state |
| **Flashlight Toggle** | Say: *"Flashlight on"* or tap torch icon | Hardware camera LED illuminates; icon glows cyan |
| **Open Installed Apps** | Say: *"Open Instagram"* or *"YouTube kholo"* | Real Instagram or YouTube application launches |
| **Search & Call Contacts** | Say: *"Call Mummy"* | Opens Android phone dialer with matched contact number |
| **Read Notifications** | Enable notification access in Settings, then say: *"Read notifications"* | JARVIS reads status bar notifications (ignoring OTPs) |
| **10 Min Reminder** | Say: *"10 minute baad reminder dena"* | Schedules alarm and alerts via high-priority notification channel |
| **Device Diagnostics** | Say: *"Phone ka model batao"* | Reports manufacturer, model, Android OS version, and free storage GB |
| **Background Assistant** | Turn on Background Service in Settings | Persistent notification appears with quick actions |

---

## 🗣️ Supported Commands Cheatsheet

### 🇮🇳 Hindi / Hinglish:
- *"Jarvis, battery kitni hai?"*
- *"Jarvis, phone charging par hai?"*
- *"Jarvis, torch chalu karo"* / *"Torch band karo"*
- *"Jarvis, Instagram kholo"* / *"WhatsApp open karo"* / *"YouTube chalao"*
- *"Jarvis, camera kholo"* / *"Settings kholo"*
- *"Jarvis, Mummy ko phone lagao"*
- *"Jarvis, latest notification batao"*
- *"Jarvis, volume badhao"* / *"Volume kam karo"*
- *"Jarvis, phone ka model batao"* / *"Storage kitni hai?"*
- *"Jarvis, 10 minute baad reminder dena"*
- *"Jarvis, abhi kitne baje hain?"* / *"Aaj ki date kya hai?"*

### 🇬🇧 English:
- *"Hey Jarvis, how much battery is left?"*
- *"Turn on flashlight"* / *"Turn off flashlight"*
- *"Open Instagram"* / *"Launch YouTube"* / *"Open Chrome"*
- *"Call [Contact Name]"*
- *"What are my notifications?"*
- *"Increase volume"* / *"Mute audio"*
- *"Show device information"*
- *"Set a reminder in 15 minutes"*
- *"Who is Tony Stark?"* / *"Tell me a joke"*
