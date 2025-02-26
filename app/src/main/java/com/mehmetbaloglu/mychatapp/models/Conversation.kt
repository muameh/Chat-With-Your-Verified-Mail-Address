package com.mehmetbaloglu.mychatapp.models

data class Conversation(
    val conversationId: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val lastMessage: String? = null,
    val lastMessageSenderId: String? = null,
    val lastMessageTimestamp: Long = System.currentTimeMillis()
)