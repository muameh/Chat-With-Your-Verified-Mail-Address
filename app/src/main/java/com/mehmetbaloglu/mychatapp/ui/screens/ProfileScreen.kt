package com.mehmetbaloglu.mychatapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mehmetbaloglu.mychatapp.navigation.AppScreens
import com.mehmetbaloglu.mychatapp.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            profileState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
            }
            profileState.error != null -> {
                Text(
                    text = "Profil yüklenemedi: ${profileState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            else -> {
                // Profil bilgileri
                Text(
                    text = "Profil",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kullanıcı Adı: ${profileState.userName ?: "Bilinmiyor"}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "E-posta: ${profileState.email ?: "Bilinmiyor"}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        navController.navigate(AppScreens.SearchOtherUsersScreen.name)
                    }, modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ) { Text("Kullanıcı Bul") }

                Button(
                    onClick = {
                        viewModel.signOut()
                        navController.navigate(AppScreens.LoginScreen.name)
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Çıkış Yap")
                }

                Button(
                    onClick = {
                        navController.navigate(AppScreens.FriendRequestsDetailScreen.name)
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Arkadaşlık istekleri")
                }
            }
        }
    }
}