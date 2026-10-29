package com.example.jarvis.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private var speechRate: Float = 1.0f
    private var speechPitch: Float = 1.0f

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })
            _isInitialized.value = true
        } else {
            _isInitialized.value = false
        }
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(speechRate)
    }

    fun setSpeechPitch(pitch: Float) {
        speechPitch = pitch.coerceIn(0.5f, 2.0f)
        tts?.setPitch(speechPitch)
    }

    fun speak(text: String, preferLanguage: String = "AUTO") {
        if (!_isInitialized.value || tts == null) return

        stop()

        // Check if text has Devanagari script or Hinglish phrases
        val hasHindiScript = text.any { it in '\u0900'..'\u097F' }
        val targetLocale = when {
            preferLanguage == "HI" || hasHindiScript -> Locale("hi", "IN")
            preferLanguage == "EN" -> Locale.US
            else -> {
                // Auto detect
                if (hasHindiScript) Locale("hi", "IN") else Locale.US
            }
        }

        try {
            val result = tts?.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English
                tts?.setLanguage(Locale.US)
            }
        } catch (e: Exception) {
            tts?.setLanguage(Locale.US)
        }

        tts?.setSpeechRate(speechRate)
        tts?.setPitch(speechPitch)

        val utteranceId = "jarvis_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
