package com.mehmetbaloglu.mychatapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mehmetbaloglu.mychatapp.models.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


data class ProfileState(
    val isLoading: Boolean = true,
    val userName: String? = null,
    val email: String? = null,
    val error: String? = null,
    val message: String? = null,
    val friends: List<Friend> = emptyList() // Arkadaş listesi
)


@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    init {
        loadProfile()
        loadFriends()
    }

    private fun loadProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _profileState.value = ProfileState(isLoading = false, error = "Kullanıcı oturumu açık değil")
            Log.e("xxProfileViewModel", "No user logged in")
            return
        }

        _profileState.value = ProfileState(isLoading = true, email = currentUser.email)

        val userId = currentUser.uid
        database.reference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("userName").getValue(String::class.java)
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        userName = userName,
                        email = currentUser.email
                    )
                    Log.d("xxProfileViewModel", "Profile loaded: userName=$userName, email=${currentUser.email}")
                }

                override fun onCancelled(error: DatabaseError) {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        email = currentUser.email,
                        error = "Profil bilgileri yüklenemedi: ${error.message}"
                    )
                    Log.e("xxProfileViewModel", "Failed to load profile: ${error.message}")
                }
            })
    }

    private fun loadFriends() {
        val currentUserId = auth.currentUser?.uid ?: return
        database.reference.child("friendRequests").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val friendList = mutableListOf<Friend>()
                    for (data in snapshot.children) {
                        val friendId = data.key ?: continue
                        val status = data.child("status").getValue(String::class.java)
                        if (status == "accepted") {
                            database.reference.child("users").child(friendId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        val userName = userSnapshot.child("userName").getValue(String::class.java) ?: "Bilinmiyor"
                                        val email = userSnapshot.child("email").getValue(String::class.java) ?: "Bilinmiyor"
                                        friendList.add(Friend(friendId, userName, email))
                                        _profileState.value = _profileState.value.copy(friends = friendList)
                                        Log.d("xxProfileViewModel", "Friend loaded: $friendId, $userName")
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("xxProfileViewModel", "Failed to load friend data: ${error.message}")
                                    }
                                })
                        }
                    }
                    if (friendList.isEmpty()) {
                        _profileState.value = _profileState.value.copy(friends = emptyList())
                        Log.d("xxProfileViewModel", "No accepted friends found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("xxProfileViewModel", "Failed to load friends: ${error.message}")
                }
            })
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _profileState.value = ProfileState(message = "Çıkış yapıldı!", isLoading = false)
                Log.d("xxProfileViewModel", "User signed out successfully")
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    message = "Çıkış yapılamadı: ${e.localizedMessage}",
                    isLoading = false
                )
                Log.e("xxProfileViewModel", "Sign out error: ${e.localizedMessage}")
            }
        }
    }

    fun updateUserName(newUserName: String) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            try {
                val userId = auth.currentUser?.uid ?: throw IllegalStateException("Kullanıcı oturumu açık değil")
                database.reference.child("users").child(userId).child("userName")
                    .setValue(newUserName).await()
                _profileState.value = ProfileState(
                    isLoading = false,
                    userName = newUserName,
                    email = auth.currentUser?.email,
                    message = "Kullanıcı adı güncellendi!"
                )
                Log.d("xxProfileViewModel", "User name updated to: $newUserName")
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    message = "Kullanıcı adı güncellenemedi: ${e.localizedMessage}",
                    isLoading = false
                )
                Log.e("xxProfileViewModel", "Update user name error: ${e.localizedMessage}")
            }
        }
    }

    fun sendPasswordResetEmail() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            try {
                val email = auth.currentUser?.email ?: throw IllegalStateException("Kullanıcı oturumu açık değil")
                auth.sendPasswordResetEmail(email).await()
                _profileState.value = _profileState.value.copy(
                    message = "Şifre sıfırlama e-postası gönderildi!",
                    isLoading = false
                )
                Log.d("xxProfileViewModel", "Password reset email sent to: $email")
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    message = "Şifre sıfırlama e-postası gönderilemedi: ${e.localizedMessage}",
                    isLoading = false
                )
                Log.e("xxProfileViewModel", "Send password reset email error: ${e.localizedMessage}")
            }
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            try {
                val userId = auth.currentUser?.uid ?: throw IllegalStateException("Kullanıcı oturumu açık değil")
                // Çift taraflı arkadaşlık kaydını sil
                database.reference.child("friendRequests").child(userId).child(friendId)
                    .removeValue().await()
                database.reference.child("friendRequests").child(friendId).child(userId)
                    .removeValue().await()

                // İlgili sohbet kaydını sil
                val conversationId = getConversationId(userId, friendId)
                database.reference.child("conversations").child(conversationId)
                    .removeValue().await()

                _profileState.value = _profileState.value.copy(
                    message = "Arkadaşlıktan çıkarıldı!",
                    isLoading = false,
                    friends = _profileState.value.friends.filter { it.userId != friendId }
                )
                Log.d("xxProfileViewModel", "Friend removed: $friendId, conversation $conversationId deleted")
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    message = "Arkadaşlıktan çıkarma başarısız: ${e.localizedMessage}",
                    isLoading = false
                )
                Log.e("xxProfileViewModel", "Remove friend error: ${e.localizedMessage}")
            }
        }
    }

    private fun getConversationId(userId: String, friendId: String): String {
        return if (userId < friendId) "${userId}_$friendId" else "${friendId}_$userId"
    }

    fun clearMessage() {
        _profileState.value = _profileState.value.copy(message = null)
    }
}