package com.mehmetbaloglu.mychatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mehmetbaloglu.mychatapp.navigation.AppNavigation
import com.mehmetbaloglu.mychatapp.navigation.AppScreens
import com.mehmetbaloglu.mychatapp.ui.theme.MyChatAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyChatAppTheme {
                val navController = rememberNavController()

                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                val startDestination = if (currentUser != null) {
                    AppScreens.ChatListScreen.name
                } else {
                    AppScreens.LoginScreen.name
                }
                AppNavigation(startDestination = startDestination, navController = navController)
            }
        }
    }
}