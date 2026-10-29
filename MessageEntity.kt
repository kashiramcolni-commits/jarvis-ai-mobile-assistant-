package com.example.jarvis.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: String = "default",
    val sender: String, // "USER" or "JARVIS"
    val text: String,
    val actionType: String? = null,
    val actionPayload: String? = null,
    val isVoice: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
