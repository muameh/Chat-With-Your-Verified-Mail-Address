package com.mehmetbaloglu.mychatapp.models

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String? = null,
    val mediaUrl: String? = null,  // Fotoğraf / Video URL
    val messageType: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    TEXT, IMAGE, VIDEO
}