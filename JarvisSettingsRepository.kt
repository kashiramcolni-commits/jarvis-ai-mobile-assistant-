package com.example.jarvis.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "jarvis_settings")

data class JarvisSettings(
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val language: String = "AUTO", // AUTO, HI, EN
    val wakeWordEnabled: Boolean = false,
    val backgroundAssistantEnabled: Boolean = false,
    val privacyFilterOtps: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
)

class JarvisSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val LANGUAGE = stringPreferencesKey("language")
        val WAKE_WORD_ENABLED = booleanPreferencesKey("wake_word_enabled")
        val BACKGROUND_ENABLED = booleanPreferencesKey("background_enabled")
        val PRIVACY_FILTER_OTPS = booleanPreferencesKey("privacy_filter_otps")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    }

    val settingsFlow: Flow<JarvisSettings> = context.dataStore.data.map { preferences ->
        JarvisSettings(
            speechRate = preferences[PreferencesKeys.SPEECH_RATE] ?: 1.0f,
            speechPitch = preferences[PreferencesKeys.SPEECH_PITCH] ?: 1.0f,
            language = preferences[PreferencesKeys.LANGUAGE] ?: "AUTO",
            wakeWordEnabled = preferences[PreferencesKeys.WAKE_WORD_ENABLED] ?: false,
            backgroundAssistantEnabled = preferences[PreferencesKeys.BACKGROUND_ENABLED] ?: false,
            privacyFilterOtps = preferences[PreferencesKeys.PRIVACY_FILTER_OTPS] ?: true,
            hapticFeedbackEnabled = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true
        )
    }

    suspend fun updateSpeechRate(rate: Float) {
        context.dataStore.edit { it[PreferencesKeys.SPEECH_RATE] = rate }
    }

    suspend fun updateSpeechPitch(pitch: Float) {
        context.dataStore.edit { it[PreferencesKeys.SPEECH_PITCH] = pitch }
    }

    suspend fun updateLanguage(lang: String) {
        context.dataStore.edit { it[PreferencesKeys.LANGUAGE] = lang }
    }

    suspend fun updateWakeWordEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.WAKE_WORD_ENABLED] = enabled }
    }

    suspend fun updateBackgroundAssistantEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BACKGROUND_ENABLED] = enabled }
    }

    suspend fun updatePrivacyFilterOtps(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PRIVACY_FILTER_OTPS] = enabled }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HAPTIC_FEEDBACK] = enabled }
    }
}
