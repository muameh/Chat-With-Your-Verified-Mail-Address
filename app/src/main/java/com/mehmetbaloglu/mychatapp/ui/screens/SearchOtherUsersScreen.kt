package com.mehmetbaloglu.mychatapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mehmetbaloglu.mychatapp.ui.viewmodels.SearchUsersViewModel

@Composable
fun SearchOtherUsersScreen(
    navController: NavController,
    viewModel: SearchUsersViewModel = hiltViewModel()
) {
    val searchState by viewModel.searchState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }

    LaunchedEffect(searchState) {
        if (searchState.message != null) {
            Toast.makeText(context, searchState.message, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta ile Ara") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Button(
            onClick = { viewModel.searchUserByEmail(email) },
            modifier = Modifier.padding(bottom = 8.dp),
            enabled = !searchState.isLoading
        ) {
            Text("Ara")
        }

        when {
            searchState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
            }
            searchState.userId != null && searchState.userName != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Kullanıcı Bulundu: ${searchState.userName}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(
                        onClick = { viewModel.sendFriendRequest(searchState.userId!!) },
                        modifier = Modifier.padding(bottom = 8.dp),
                        enabled = !searchState.isLoading
                    ) {
                        Text("İstek Gönder")
                    }
                }
            }
            searchState.message != null -> {
                Text(
                    text = searchState.message!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}