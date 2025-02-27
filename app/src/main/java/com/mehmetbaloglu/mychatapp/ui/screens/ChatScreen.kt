package com.mehmetbaloglu.mychatapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mehmetbaloglu.mychatapp.models.Message
import com.mehmetbaloglu.mychatapp.ui.viewmodels.ChatViewModel

@Composable
fun ChatScreen(
    navController: NavController,
    friendId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState() // Mesajları ViewModel'dan al
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(friendId) {
        viewModel.loadMessages(friendId) // friendId ile mesajları yükle
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Mesaj listesi
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true // En yeni mesajlar altta
        ) {
            items(messages) { message ->
                MessageItem(message = message)
            }
        }

        // Mesaj yazma alanı
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Mesaj yaz") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(friendId, messageText)
                        messageText = "" // Gönderdikten sonra sıfırla
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Gönder")
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isSentByCurrentUser = message.senderId == currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .wrapContentWidth()
        ) {
            Text(
                text = message.text ?: "",
                modifier = Modifier.padding(8.dp),
                color = if (isSentByCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}