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

data class SearchState(
    val isLoading: Boolean = false,
    val userId: String? = null,
    val userName: String? = null,
    val message: String? = null
)

@HiltViewModel
class SearchUsersViewModel @Inject constructor() : ViewModel() {
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    fun searchUserByEmail(email: String) {
        viewModelScope.launch {
            _searchState.value = SearchState(isLoading = true)
            try {
                // Firebase Authentication'da e-posta ile kullanıcı bulma (doğrudan UID alamıyoruz, veritabanını kullanıyoruz)
                database.reference.child("users")
                    .orderByChild("email")
                    .equalTo(email)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (child in snapshot.children) {
                                    val userId = child.key
                                    val userName =
                                        child.child("userName").getValue(String::class.java)
                                    if (userId != null && userName != null) {
                                        _searchState.value = SearchState(
                                            isLoading = false,
                                            userId = userId,
                                            userName = userName
                                        )
                                        Log.d(
                                            "xxSearchUsersViewModel",
                                            "User found: $userId, $userName"
                                        )
                                        return
                                    }
                                }
                            }
                            _searchState.value = SearchState(
                                isLoading = false,
                                message = "Kullanıcı bulunamadı!"
                            )
                            Log.d("xxSearchUsersViewModel", "No user found for email: $email")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            _searchState.value = SearchState(
                                isLoading = false,
                                message = "Arama hatası: ${error.message}"
                            )
                            Log.e("xxSearchUsersViewModel", "Search failed: ${error.message}")
                        }
                    })
            } catch (e: Exception) {
                _searchState.value = SearchState(
                    isLoading = false,
                    message = "Hata: ${e.localizedMessage}"
                )
                Log.e("xxSearchUsersViewModel", "Search error: ${e.localizedMessage}")
            }
        }
    }

    fun sendFriendRequest(receiverId: String) {
        viewModelScope.launch {
            _searchState.value = _searchState.value.copy(isLoading = true)
            try {
                val senderId = auth.currentUser?.uid
                    ?: throw IllegalStateException("Kullanıcı oturumu açık değil")
                database.reference.child("friendRequests")
                    .child(receiverId)
                    .child(senderId)
                    .setValue(mapOf("status" to "pending"))
                    .await()
                _searchState.value = SearchState(
                    isLoading = false,
                    userId = receiverId,
                    userName = _searchState.value.userName,
                    message = "Arkadaşlık isteği gönderildi!"
                )
                Log.d("xxSearchUsersViewModel", "Friend request sent to $receiverId from $senderId")
            } catch (e: Exception) {
                _searchState.value = SearchState(
                    isLoading = false,
                    userId = receiverId,
                    userName = _searchState.value.userName,
                    message = "İstek gönderilemedi: ${e.localizedMessage}"
                )
                Log.e("xxSearchUsersViewModel", "Send friend request error: ${e.localizedMessage}")
            }
        }
    }

    fun clearMessage() {
        _searchState.value = _searchState.value.copy(message = null)
    }
}