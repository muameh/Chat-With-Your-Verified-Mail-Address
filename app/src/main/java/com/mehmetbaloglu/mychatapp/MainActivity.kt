package com.mehmetbaloglu.mychatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                var currentUser by remember { mutableStateOf(auth.currentUser) }

                LaunchedEffect(Unit) {
                    auth.addAuthStateListener { firebaseAuth ->
                        currentUser = firebaseAuth.currentUser
                        if (currentUser != null && currentUser!!.isEmailVerified) {
                            navController.navigate(AppScreens.ChatListScreen.name) {
                                popUpTo(AppScreens.LoginScreen.name) { inclusive = true }
                            }
                        } else {
                            navController.navigate(AppScreens.LoginScreen.name) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    }
                }

                AppNavigation(
                    startDestination = AppScreens.LoginScreen.name,
                    navController = navController
                )
            }
        }
    }
}