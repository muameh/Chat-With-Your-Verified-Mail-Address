package com.mehmetbaloglu.mychatapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mehmetbaloglu.mychatapp.models.Conversation
import com.mehmetbaloglu.mychatapp.models.Resource
import com.mehmetbaloglu.mychatapp.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor() : ViewModel() {
    private val _homeState = MutableStateFlow<Resource<String>>(Resource())
    val homeState: StateFlow<Resource<String>> = _homeState

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    private val _isLoading = MutableStateFlow(true) // Yükleme durumu
    val isLoading: StateFlow<Boolean> = _isLoading

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadConversations()
    }

    fun logOut() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signOut()
                _homeState.value = Resource(data = "Çıkış yapıldı!", isLoading = false)
                Log.d("xxHomeViewModel", "User logged out successfully")
            } catch (e: Exception) {
                _homeState.value = Resource(message = "Çıkış hatası: ${e.localizedMessage}", isLoading = false)
                Log.e("xxHomeViewModel", "Logout error: ${e.localizedMessage}")
            }
        }
    }

    fun clearMessage() {
        _homeState.value = _homeState.value.copy(message = null)
    }

    private fun loadConversations() {
        val currentUserId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        database.reference.child("conversations")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val conversationList = mutableListOf<Conversation>()
                    for (data in snapshot.children) {
                        val conversationId = data.key ?: continue
                        val user1Id = data.child("user1Id").getValue(String::class.java) ?: continue
                        val user2Id = data.child("user2Id").getValue(String::class.java) ?: continue

                        if (user1Id == currentUserId || user2Id == currentUserId) {
                            val friendId = if (user1Id == currentUserId) user2Id else user1Id
                            val lastMessage = data.child("lastMessage").getValue(String::class.java)
                            val lastMessageSenderId = data.child("lastMessageSenderId").getValue(String::class.java)
                            val lastMessageTimestamp = data.child("lastMessageTimestamp").getValue(Long::class.java)

                            conversationList.add(
                                Conversation(
                                    conversationId = conversationId,
                                    friendId = friendId,
                                    lastMessage = lastMessage,
                                    lastMessageSenderId = lastMessageSenderId,
                                    lastMessageTimestamp = lastMessageTimestamp
                                )
                            )
                        }
                    }
                    _conversations.value = conversationList.sortedByDescending { it.lastMessageTimestamp }
                    _isLoading.value = false
                    Log.d("xxHomeViewModel", "Conversations loaded: ${conversationList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("xxHomeViewModel", "Failed to load conversations: ${error.message}")
                }
            })
    }
}