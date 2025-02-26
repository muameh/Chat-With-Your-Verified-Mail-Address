package com.mehmetbaloglu.mychatapp.models

data class IsOnline(
    val userId: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)