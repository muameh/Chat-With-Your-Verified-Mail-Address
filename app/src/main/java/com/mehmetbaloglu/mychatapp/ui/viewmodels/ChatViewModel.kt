package com.mehmetbaloglu.mychatapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mehmetbaloglu.mychatapp.models.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _chatState = MutableStateFlow<ChatState>(ChatState())
    val chatState: StateFlow<ChatState> = _chatState

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    data class ChatState(
        val isLoading: Boolean = true,
        val error: String? = null,
        val isFriend: Boolean = false // Arkadaşlık durumunu takip et
    )

    private fun getConversationId(currentUserId: String, friendId: String): String {
        return if (currentUserId < friendId) "${currentUserId}_$friendId" else "${friendId}_$currentUserId"
    }

    fun loadMessages(friendId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        _chatState.value = ChatState(isLoading = true)

        // Arkadaşlık durumunu kontrol et
        database.reference.child("friendRequests").child(currentUserId).child(friendId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.child("status").getValue(String::class.java)
                    if (status == "accepted") {
                        _chatState.value = ChatState(isFriend = true)
                        val conversationId = getConversationId(currentUserId, friendId)
                        database.reference.child("conversations").child(conversationId).child("messages")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val messageList = mutableListOf<Message>()
                                    for (data in snapshot.children) {
                                        val message = data.getValue(Message::class.java)
                                        message?.let { messageList.add(it) }
                                    }
                                    _messages.value = messageList.sortedByDescending { it.timestamp }
                                    _chatState.value = ChatState(isLoading = false, isFriend = true)
                                    Log.d("xxChatViewModel", "Messages loaded: ${messageList.size}")
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    _chatState.value = ChatState(
                                        isLoading = false,
                                        error = "Mesajlar yüklenemedi: ${error.message}",
                                        isFriend = true
                                    )
                                    Log.e("xxChatViewModel", "Failed to load messages: ${error.message}")
                                }
                            })
                    } else {
                        _chatState.value = ChatState(
                            isLoading = false,
                            error = "Bu kullanıcı ile mesajlaşamazsınız, arkadaş değilsiniz.",
                            isFriend = false
                        )
                        Log.d("xxChatViewModel", "Not friends with: $friendId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _chatState.value = ChatState(
                        isLoading = false,
                        error = "Arkadaşlık durumu kontrol edilemedi: ${error.message}"
                    )
                    Log.e("xxChatViewModel", "Failed to check friendship: ${error.message}")
                }
            })
    }

    fun sendMessage(friendId: String, text: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Arkadaşlık durumunu kontrol et
                val snapshot = database.reference.child("friendRequests").child(currentUserId).child(friendId)
                    .get().await()
                val status = snapshot.child("status").getValue(String::class.java)
                if (status != "accepted") {
                    _chatState.value = ChatState(
                        isLoading = false,
                        error = "Bu kullanıcı ile mesajlaşamazsınız, arkadaş değilsiniz.",
                        isFriend = false
                    )
                    Log.d("xxChatViewModel", "Cannot send message, not friends with: $friendId")
                    return@launch
                }

                val conversationId = getConversationId(currentUserId, friendId)
                val message = Message(
                    messageId = database.reference.push().key ?: "",
                    senderId = currentUserId,
                    receiverId = friendId,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )

                database.reference.child("conversations").child(conversationId).child("messages")
                    .child(message.messageId).setValue(message).await()
                database.reference.child("conversations").child(conversationId).updateChildren(
                    mapOf(
                        "lastMessage" to message.text,
                        "lastMessageSenderId" to message.senderId,
                        "lastMessageTimestamp" to message.timestamp,
                        "user1Id" to currentUserId,
                        "user2Id" to friendId
                    )
                ).await()
                Log.d("xxChatViewModel", "Message sent to: $friendId")
            } catch (e: Exception) {
                Log.e("xxChatViewModel", "Failed to send message: ${e.localizedMessage}")
            }
        }
    }
}