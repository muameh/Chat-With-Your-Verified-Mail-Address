package com.mehmetbaloglu.mychatapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mehmetbaloglu.mychatapp.navigation.AppScreens
import com.mehmetbaloglu.mychatapp.ui.viewmodels.ChatListViewModel

@Composable
fun ChatListScreen(
    navController: NavController,
    chatListViewModel: ChatListViewModel= hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // innerPadding yerine sabit padding, geçici çözüm
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Chat List Screen")
        Button(
            onClick = {
                chatListViewModel.logOut() // Çıkış yap
                navController.navigate(AppScreens.LoginScreen.name) {
                    popUpTo(AppScreens.ChatListScreen.name) { inclusive = true } // ChatListScreen'i yığından kaldır
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Log Off")
        }
    }
}