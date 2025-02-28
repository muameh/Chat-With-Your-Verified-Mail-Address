package com.mehmetbaloglu.mychatapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mehmetbaloglu.mychatapp.models.Conversation
import com.mehmetbaloglu.mychatapp.navigation.AppScreens
import com.mehmetbaloglu.mychatapp.ui.viewmodels.ChatListViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val homeState by viewModel.homeState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    var showFriendsDialog by remember { mutableStateOf(false) }

    if (currentUserId == null) {
        LaunchedEffect(Unit) {
            navController.navigate(AppScreens.LoginScreen.name) {
                popUpTo(AppScreens.ChatListScreen.name) { inclusive = true }
            }
        }
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showFriendsDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Mesaj")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Sohbetler",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp)
                    )
                }
                conversations.isEmpty() -> {
                    Text(
                        text = homeState.message ?: "Henüz sohbet yok.",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(conversations) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                onClick = {
                                    navController.navigate("${AppScreens.ChatScreen.name}/${conversation.friendId}")
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { navController.navigate(AppScreens.ProfileScreen.name) },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            ) {
                Text("Profile")
            }

            // Arkadaşlar Dialogu
            if (showFriendsDialog) {
                AlertDialog(
                    onDismissRequest = { showFriendsDialog = false },
                    title = { Text("Arkadaşlarım") },
                    text = {
                        if (friends.isEmpty()) {
                            Text("Henüz arkadaşın yok.")
                        } else {
                            LazyColumn {
                                items(friends) { friend ->
                                    FriendItemforChatList(
                                        friend = friend,
                                        onClick = {
                                            navController.navigate("${AppScreens.ChatScreen.name}/${friend.userId}")
                                            showFriendsDialog = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFriendsDialog = false }) {
                            Text("Kapat")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            conversation.friendUserName?.let {
                Text(
                    text = it, // Burada friendId yerine kullanıcı adını göstermek daha iyi olabilir
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = conversation.lastMessage ?: "Mesaj yok",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = conversation.lastMessageTimestamp?.let { formatTimestamp(it) } ?: "",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun FriendItemforChatList(
    friend: com.mehmetbaloglu.mychatapp.models.Friend,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = friend.userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = friend.email,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}