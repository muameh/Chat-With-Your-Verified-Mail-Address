package com.mehmetbaloglu.mychatapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mehmetbaloglu.mychatapp.ui.viewmodels.FriendRequestsViewModel

@Composable
fun FriendRequestsDetailScreen(
    navController: NavController,
    viewModel: FriendRequestsViewModel = hiltViewModel()
) {
    val requestsState by viewModel.requestsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Arkadaşlık İstekleri",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when {
            requestsState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
            }
            requestsState.error != null -> {
                Text(
                    text = "Hata: ${requestsState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            requestsState.requests.isEmpty() -> {
                Text(
                    text = "Henüz arkadaşlık isteği yok.",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(requestsState.requests) { request ->
                        FriendRequestItem(
                            senderName = request.userName,
                            senderEmail = request.email,
                            onAccept = { viewModel.acceptFriendRequest(request.userId) },
                            onReject = { viewModel.rejectFriendRequest(request.userId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    senderName: String,
    senderEmail: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                    text = senderName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = senderEmail,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Kabul Et")
                }
                OutlinedButton(
                    onClick = onReject
                ) {
                    Text("Reddet")
                }
            }
        }
    }
}