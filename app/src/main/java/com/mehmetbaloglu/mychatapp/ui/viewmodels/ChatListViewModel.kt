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
import com.mehmetbaloglu.mychatapp.models.Friend
import com.mehmetbaloglu.mychatapp.models.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor() : ViewModel() {
    private val _homeState = MutableStateFlow<Resource<String>>(Resource())
    val homeState: StateFlow<Resource<String>> = _homeState

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadConversations()
        loadFriends()
    }

    private fun loadConversations() {
        val currentUserId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        database.reference.child("conversations")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val conversationList = mutableListOf<Conversation>()
                    val pendingCount = snapshot.children.count {
                        val user1Id = it.child("user1Id").getValue(String::class.java)
                        val user2Id = it.child("user2Id").getValue(String::class.java)
                        user1Id == currentUserId || user2Id == currentUserId
                    }
                    var loadedCount = 0

                    if (pendingCount == 0) {
                        _conversations.value = emptyList()
                        _isLoading.value = false
                        Log.d("xxChatListViewModel", "No conversations found")
                        return
                    }

                    for (data in snapshot.children) {
                        val conversationId = data.key ?: continue
                        val user1Id = data.child("user1Id").getValue(String::class.java) ?: continue
                        val user2Id = data.child("user2Id").getValue(String::class.java) ?: continue

                        if (user1Id == currentUserId || user2Id == currentUserId) {
                            val friendId = if (user1Id == currentUserId) user2Id else user1Id
                            val lastMessage = data.child("lastMessage").getValue(String::class.java)
                            val lastMessageSenderId = data.child("lastMessageSenderId").getValue(String::class.java)
                            val lastMessageTimestamp = data.child("lastMessageTimestamp").getValue(Long::class.java)

                            // Arkadaşın bilgilerini users tablosundan al
                            database.reference.child("users").child(friendId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        val friendUserName = userSnapshot.child("userName").getValue(String::class.java) ?: "Bilinmiyor"
                                        val friendEmail = userSnapshot.child("email").getValue(String::class.java) ?: "Bilinmiyor"

                                        conversationList.add(
                                            Conversation(
                                                conversationId = conversationId,
                                                friendId = friendId,
                                                friendUserName = friendUserName,
                                                friendEmail = friendEmail,
                                                lastMessage = lastMessage,
                                                lastMessageSenderId = lastMessageSenderId,
                                                lastMessageTimestamp = lastMessageTimestamp
                                            )
                                        )
                                        loadedCount++

                                        if (loadedCount == pendingCount) {
                                            _conversations.value = conversationList.sortedByDescending { it.lastMessageTimestamp }
                                            _isLoading.value = false
                                            Log.d("xxChatListViewModel", "Conversations loaded: ${conversationList.size}")
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("xxChatListViewModel", "Failed to load friend data for $friendId: ${error.message}")
                                        loadedCount++
                                        if (loadedCount == pendingCount) {
                                            _conversations.value = conversationList.sortedByDescending { it.lastMessageTimestamp }
                                            _isLoading.value = false
                                            Log.d("xxChatListViewModel", "Conversations partially loaded: ${conversationList.size}")
                                        }
                                    }
                                })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("xxChatListViewModel", "Failed to load conversations: ${error.message}")
                    _isLoading.value = false
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

                                        val acceptedCount = snapshot.children.count { it.child("status").value == "accepted" }
                                        if (friendList.size == acceptedCount) {
                                            _friends.value = friendList
                                            Log.d("xxChatListViewModel", "Friends loaded: ${friendList.size}")
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("xxChatListViewModel", "Failed to load friend data: ${error.message}")
                                    }
                                })
                        }
                    }
                    if (snapshot.children.none { it.child("status").value == "accepted" }) {
                        _friends.value = emptyList()
                        Log.d("xxChatListViewModel", "No accepted friends found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("xxChatListViewModel", "Failed to load friends: ${error.message}")
                }
            })
    }
}