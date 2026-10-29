package com.example.jarvis.device

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings

class VolumeController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun adjustVolume(increase: Boolean): String {
        if (audioManager == null) return "Audio service is not accessible."

        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            AudioManager.FLAG_SHOW_UI
        )

        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val percent = (currentVol * 100) / maxVol

        return if (increase) {
            "Volume increased to $percent percent."
        } else {
            "Volume decreased to $percent percent."
        }
    }

    fun mute(): String {
        if (audioManager == null) return "Audio service not accessible."
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_MUTE,
            AudioManager.FLAG_SHOW_UI
        )
        return "Media audio muted."
    }

    fun openSoundSettings() {
        val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
