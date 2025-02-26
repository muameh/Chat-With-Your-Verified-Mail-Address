package com.mehmetbaloglu.mychatapp.models

data class User(
    val userId: String = "",
    val email: String = "",
    val username: String = "",
    val profileImageUrl: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val friends: List<String> = emptyList(), // Opsiyonel: Arkadaş UID'leri
    val requests: List<String> = emptyList() // Opsiyonel: Gelen istek UID'leri
)