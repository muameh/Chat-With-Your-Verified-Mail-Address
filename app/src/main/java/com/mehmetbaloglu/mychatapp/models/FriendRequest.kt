package com.mehmetbaloglu.mychatapp.models

// Arkadaşlık İsteği Modeli
data class FriendRequest(
    val requestId: String = "", // Benzersiz ID için
    val requesterId: String = "",
    val requesterEmail: String = "",
    val receiverId: String = "",
    val status: FriendRequestStatus = FriendRequestStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

enum class FriendRequestStatus {
    PENDING, ACCEPTED, REJECTED
}

