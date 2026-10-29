package com.example.jarvis.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class SpeechState {
    object Idle : SpeechState()
    object Ready : SpeechState()
    object Listening : SpeechState()
    object Processing : SpeechState()
    data class Error(val message: String, val canFallbackToSystemDialog: Boolean = false) : SpeechState()
    data class Success(val recognizedText: String) : SpeechState()
}

class SpeechRecognitionManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _rmsAudioLevel = MutableStateFlow(0f)
    val rmsAudioLevel: StateFlow<Float> = _rmsAudioLevel.asStateFlow()

    fun isAvailable(): Boolean {
        return try {
            SpeechRecognizer.isRecognitionAvailable(context)
        } catch (e: Exception) {
            false
        }
    }

    fun buildSpeechRecognizerIntent(languageCode: String = "AUTO"): Intent {
        val selectedLocale = when (languageCode) {
            "HI" -> Locale("hi", "IN").toLanguageTag()
            "EN" -> Locale("en", "IN").toLanguageTag()
            else -> Locale.getDefault().toLanguageTag()
        }

        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLocale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLocale)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, selectedLocale)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak command to JARVIS...")
        }
    }

    fun startListening(languageCode: String = "AUTO", onFallbackRequired: (() -> Unit)? = null) {
        mainHandler.post {
            if (!isAvailable()) {
                _speechState.value = SpeechState.Error("Background speech recognition service unavailable. Using system voice dialog.", canFallbackToSystemDialog = true)
                onFallbackRequired?.invoke()
                return@post
            }

            stopListening()

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener(onFallbackRequired))
                }

                val intent = buildSpeechRecognizerIntent(languageCode)
                speechRecognizer?.startListening(intent)
                _speechState.value = SpeechState.Listening
            } catch (e: Exception) {
                _speechState.value = SpeechState.Error("Voice input initialization failed: ${e.message}", canFallbackToSystemDialog = true)
                onFallbackRequired?.invoke()
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                // Ignore cleanup error
            }
            _rmsAudioLevel.value = 0f
            if (_speechState.value is SpeechState.Listening) {
                _speechState.value = SpeechState.Idle
            }
        }
    }

    fun onExternalSpeechResult(recognizedText: String) {
        _speechState.value = SpeechState.Success(recognizedText)
    }

    private fun createListener(onFallbackRequired: (() -> Unit)?): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _speechState.value = SpeechState.Ready
            }

            override fun onBeginningOfSpeech() {
                _speechState.value = SpeechState.Listening
            }

            override fun onRmsChanged(rmsdB: Float) {
                _rmsAudioLevel.value = (rmsdB + 2f).coerceIn(0f, 12f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _speechState.value = SpeechState.Processing
                _rmsAudioLevel.value = 0f
            }

            override fun onError(error: Int) {
                _rmsAudioLevel.value = 0f
                var canFallback = false
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> {
                        canFallback = true
                        "Speech client busy or unavailable. Launching voice input..."
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                    SpeechRecognizer.ERROR_NETWORK -> "Network required for speech recognition"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out. Please try again."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap mic and try again, Sir."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        canFallback = true
                        "Speech service busy"
                    }
                    SpeechRecognizer.ERROR_SERVER -> {
                        canFallback = true
                        "Speech server error"
                    }
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                    else -> "Speech recognition error ($error)"
                }

                _speechState.value = SpeechState.Error(errorMessage, canFallback)

                if (canFallback) {
                    onFallbackRequired?.invoke()
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _speechState.value = SpeechState.Success(matches[0])
                } else {
                    _speechState.value = SpeechState.Error("No command heard.")
                }
                _rmsAudioLevel.value = 0f
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
