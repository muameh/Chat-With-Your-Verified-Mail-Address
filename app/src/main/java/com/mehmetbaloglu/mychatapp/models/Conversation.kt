package com.mehmetbaloglu.mychatapp.models

data class Conversation(
    val conversationId: String = "",
    val friendId: String = "",
    val lastMessage: String? = null,
    val lastMessageSenderId: String? = null,
    val lastMessageTimestamp: Long? = null
)