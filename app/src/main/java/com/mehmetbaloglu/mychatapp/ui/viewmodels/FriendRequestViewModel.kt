package com.mehmetbaloglu.mychatapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class FriendRequest(
    val userId: String,
    val userName: String,
    val email: String
)

data class FriendRequestsState(
    val isLoading: Boolean = true,
    val requests: List<FriendRequest> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FriendRequestsViewModel @Inject constructor() : ViewModel() {
    private val _requestsState = MutableStateFlow(FriendRequestsState())
    val requestsState: StateFlow<FriendRequestsState> = _requestsState

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    init {
        loadFriendRequests()
    }

    private fun loadFriendRequests() {
        val currentUserId = auth.currentUser?.uid ?: run {
            _requestsState.value = FriendRequestsState(
                isLoading = false,
                error = "Kullanıcı oturumu açık değil"
            )
            Log.e("xxFriendRequestsViewModel", "No current user")
            return
        }
        _requestsState.value = FriendRequestsState(isLoading = true)
        Log.d("xxFriendRequestsViewModel", "Loading friend requests for user: $currentUserId")

        database.reference.child("friendRequests").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(requestsSnapshot: DataSnapshot) {
                    Log.d("xxFriendRequestsViewModel", "friendRequests onDataChange: ${requestsSnapshot.childrenCount} children")
                    val requestList = mutableListOf<FriendRequest>()

                    if (!requestsSnapshot.exists()) {
                        _requestsState.value = FriendRequestsState(isLoading = false)
                        Log.d("xxFriendRequestsViewModel", "No friend requests found")
                        return
                    }

                    val pendingCount = requestsSnapshot.children.count {
                        it.child("status").value == "pending" && it.key != currentUserId
                    }
                    Log.d("xxFriendRequestsViewModel", "Pending requests count (excluding self): $pendingCount")
                    var loadedCount = 0

                    if (pendingCount == 0) {
                        _requestsState.value = FriendRequestsState(isLoading = false)
                        Log.d("xxFriendRequestsViewModel", "No valid pending requests")
                        return
                    }

                    requestsSnapshot.children.forEach { data ->
                        val senderId = data.key ?: return@forEach
                        val status = data.child("status").getValue(String::class.java)
                        Log.d("xxFriendRequestsViewModel", "Processing senderId: $senderId, status: $status")

                        if (status == "pending" && senderId != currentUserId) { // Kendine istekleri hariç tut
                            database.reference.child("users").child(senderId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        Log.d("xxFriendRequestsViewModel", "User data for $senderId: ${userSnapshot.value}")
                                        val userName = userSnapshot.child("userName").getValue(String::class.java) ?: "Bilinmiyor"
                                        val email = userSnapshot.child("email").getValue(String::class.java) ?: "Bilinmiyor"
                                        requestList.add(FriendRequest(senderId, userName, email))
                                        loadedCount++

                                        Log.d("xxFriendRequestsViewModel", "Loaded request $loadedCount/$pendingCount for $senderId")
                                        if (loadedCount == pendingCount) {
                                            _requestsState.value = FriendRequestsState(
                                                isLoading = false,
                                                requests = requestList
                                            )
                                            Log.d("xxFriendRequestsViewModel", "Requests loaded: ${requestList.size}")
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("xxFriendRequestsViewModel", "Failed to load user data for $senderId: ${error.message}")
                                        loadedCount++
                                        if (loadedCount == pendingCount) {
                                            _requestsState.value = FriendRequestsState(
                                                isLoading = false,
                                                requests = requestList,
                                                error = "Bazı gönderici bilgileri yüklenemedi: ${error.message}"
                                            )
                                            Log.d("xxFriendRequestsViewModel", "Requests partially loaded: ${requestList.size}")
                                        }
                                    }
                                })
                        } else {
                            loadedCount++
                            if (loadedCount == requestsSnapshot.children.count { it.key != currentUserId }) {
                                _requestsState.value = FriendRequestsState(
                                    isLoading = false,
                                    requests = requestList
                                )
                                Log.d("xxFriendRequestsViewModel", "Requests loaded (no valid pending): ${requestList.size}")
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _requestsState.value = FriendRequestsState(
                        isLoading = false,
                        error = "İstekler yüklenemedi: ${error.message}"
                    )
                    Log.e("xxFriendRequestsViewModel", "Failed to load requests: ${error.message}")
                }
            })
    }

    fun acceptFriendRequest(senderId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                database.reference.child("friendRequests").child(currentUserId).child(senderId)
                    .child("status").setValue("accepted").await()

                // Karşılıklı arkadaşlık kaydı (simetrik)
                database.reference.child("friendRequests").child(senderId).child(currentUserId)
                    .child("status").setValue("accepted").await()

                Log.d("xxFriendRequestsViewModel", "Friend request accepted: $senderId")
            } catch (e: Exception) {
                _requestsState.value = _requestsState.value.copy(
                    error = "İstek kabul edilemedi: ${e.localizedMessage}"
                )
                Log.e("xxFriendRequestsViewModel", "Accept error: ${e.localizedMessage}")
            }
        }
    }

    fun rejectFriendRequest(senderId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                database.reference.child("friendRequests").child(currentUserId).child(senderId)
                    .child("status").setValue("rejected").await()

                Log.d("xxFriendRequestsViewModel", "Friend request rejected: $senderId")
            } catch (e: Exception) {
                _requestsState.value = _requestsState.value.copy(
                    error = "İstek reddedilemedi: ${e.localizedMessage}"
                )
                Log.e("xxFriendRequestsViewModel", "Reject error: ${e.localizedMessage}")
            }
        }
    }
}