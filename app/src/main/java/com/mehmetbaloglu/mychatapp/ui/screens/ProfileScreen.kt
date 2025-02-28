package com.mehmetbaloglu.mychatapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mehmetbaloglu.mychatapp.models.Friend
import com.mehmetbaloglu.mychatapp.navigation.AppScreens
import com.mehmetbaloglu.mychatapp.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val profileState by viewModel.profileState.collectAsState()
    val context = LocalContext.current

    var newUserName by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showFriendsDialog by remember { mutableStateOf(false) }
    var showRemoveConfirmDialog by remember { mutableStateOf(false) }
    var friendToRemove by remember { mutableStateOf<Friend?>(null) }

    LaunchedEffect(profileState.message) {
        if (profileState.message != null) {
            Log.d("xxProfileScreen", "Message: ${profileState.message}")
            Toast.makeText(context, profileState.message, Toast.LENGTH_LONG).show()
            if (profileState.message == "Çıkış yapıldı!") {
                navController.navigate(AppScreens.LoginScreen.name) {
                    popUpTo(AppScreens.ProfileScreen.name) { inclusive = true }
                }
            }
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(profileState.error) {
        if (profileState.error != null) {
            formError = profileState.error
            Toast.makeText(context, formError, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            profileState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
            }
            profileState.error != null -> {
                Text(
                    text = "${profileState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            else -> {
                Text(
                    text = "Profil",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kullanıcı Adı: ${profileState.userName ?: "Bilinmiyor"}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "E-posta: ${profileState.email ?: "Bilinmiyor"}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Kullanıcı Adı Güncelleme Butonu
                Button(
                    onClick = { showUpdateDialog = true },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Kullanıcı Adını Güncelle")
                }

                // Şifre Sıfırlama
                Button(
                    onClick = { viewModel.sendPasswordResetEmail() },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Şifre Sıfırlama E-postası Gönder")
                }

                // Arkadaşlar Butonu
                Button(
                    onClick = { showFriendsDialog = true },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Arkadaşlar")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mevcut Butonlar
                Button(
                    onClick = { navController.navigate(AppScreens.SearchOtherUsersScreen.name) },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Kullanıcı Bul")
                }

                Button(
                    onClick = { navController.navigate(AppScreens.FriendRequestsDetailScreen.name) },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Arkadaşlık İstekleri")
                }

                Button(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Çıkış Yap")
                }

                formError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                // Kullanıcı Adı Güncelleme Dialogu
                if (showUpdateDialog) {
                    AlertDialog(
                        onDismissRequest = { showUpdateDialog = false },
                        title = { Text("Kullanıcı Adını Güncelle") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = newUserName,
                                    onValueChange = { newUserName = it },
                                    label = { Text("Yeni Kullanıcı Adı") },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = formError != null && newUserName.isBlank()
                                )
                                formError?.let {
                                    Text(
                                        text = it,
                                        color = Color.Red,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newUserName.isBlank()) {
                                        formError = "Kullanıcı adı boş olamaz!"
                                        Toast.makeText(context, formError, Toast.LENGTH_LONG).show()
                                    } else {
                                        viewModel.updateUserName(newUserName)
                                        newUserName = ""
                                        showUpdateDialog = false
                                    }
                                }
                            ) {
                                Text("Güncelle")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showUpdateDialog = false
                                newUserName = ""
                            }) {
                                Text("Vazgeç")
                            }
                        }
                    )
                }

                // Arkadaşlar Dialogu
                if (showFriendsDialog) {
                    AlertDialog(
                        onDismissRequest = { showFriendsDialog = false },
                        title = { Text("Arkadaşlar") },
                        text = {
                            if (profileState.friends.isEmpty()) {
                                Text("Henüz arkadaşın yok.")
                            } else {
                                LazyColumn {
                                    items(profileState.friends) { friend ->
                                        FriendItem(
                                            friend = friend,
                                            onRemoveClick = {
                                                friendToRemove = friend
                                                showRemoveConfirmDialog = true
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

                // Arkadaşlıktan Çıkarma Onay Dialogu
                if (showRemoveConfirmDialog && friendToRemove != null) {
                    AlertDialog(
                        onDismissRequest = { showRemoveConfirmDialog = false },
                        title = { Text("Emin misiniz?") },
                        text = { Text("${friendToRemove!!.userName} adlı arkadaşınızı çıkarmak istediğinize emin misiniz?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.removeFriend(friendToRemove!!.userId)
                                    showRemoveConfirmDialog = false
                                    friendToRemove = null
                                }
                            ) {
                                Text("Evet")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showRemoveConfirmDialog = false
                                friendToRemove = null
                            }) {
                                Text("Hayır")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FriendItem(
    friend: Friend,
    onRemoveClick: () -> Unit
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
                    text = friend.userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = friend.email,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = onRemoveClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Arkadaşlıktan Çıkar", color = Color.White)
            }
        }
    }
}