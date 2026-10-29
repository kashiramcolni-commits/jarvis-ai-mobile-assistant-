package com.example.jarvis.voice

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class WakeWordStatus {
    object Inactive : WakeWordStatus()
    object Standby : WakeWordStatus()
    object Triggered : WakeWordStatus()
    data class Unavailable(val reason: String) : WakeWordStatus()
}

class WakeWordManager(private val context: Context) {

    private val _status = MutableStateFlow<WakeWordStatus>(WakeWordStatus.Inactive)
    val status: StateFlow<WakeWordStatus> = _status.asStateFlow()

    private val wakeKeywords = listOf("hey jarvis", "jarvis", "हे जारविस", "जारविस", "ok jarvis")

    fun isWakeWordContained(phrase: String): Boolean {
        val lower = phrase.lowercase().trim()
        return wakeKeywords.any { lower.contains(it) }
    }

    fun stripWakeWord(phrase: String): String {
        var clean = phrase
        for (kw in wakeKeywords) {
            clean = clean.replace(Regex("(?i)$kw[,\\s]*"), "")
        }
        return clean.trim()
    }

    fun startListening() {
        _status.value = WakeWordStatus.Standby
    }

    fun stopListening() {
        _status.value = WakeWordStatus.Inactive
    }

    fun notifyTriggered() {
        _status.value = WakeWordStatus.Triggered
    }
}
