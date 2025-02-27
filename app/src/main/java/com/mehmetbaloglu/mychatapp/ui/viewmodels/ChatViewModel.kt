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
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Sabit sıralı conversationId oluşturma
    private fun getConversationId(currentUserId: String, friendId: String): String {
        return if (currentUserId < friendId) "${currentUserId}_${friendId}"
        else "${friendId}_${currentUserId}"
    }

    fun loadMessages(friendId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
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
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("xxChatViewModel", "Failed to load messages: ${error.message}")
                }
            })
    }

    fun sendMessage(friendId: String, text: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val conversationId = getConversationId(currentUserId, friendId)

        val message = Message(
            messageId = database.reference.push().key ?: "",
            senderId = currentUserId,
            receiverId = friendId,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                database.reference.child("conversations").child(conversationId).child("messages")
                    .child(message.messageId).setValue(message)
                // Son mesajı güncelle
                database.reference.child("conversations").child(conversationId).updateChildren(
                    mapOf(
                        "lastMessage" to message.text,
                        "lastMessageSenderId" to message.senderId,
                        "lastMessageTimestamp" to message.timestamp,
                        "user1Id" to currentUserId,
                        "user2Id" to friendId
                    )
                )
            } catch (e: Exception) {
                Log.e("xxChatViewModel", "Failed to send message: ${e.localizedMessage}")
            }
        }
    }
}