package com.mehmetbaloglu.mychatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
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
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContent {
            MyChatAppTheme {
                val navController = rememberNavController()
                var currentUser by remember { mutableStateOf(auth.currentUser) }

                // Auth state listener'ı DisposableEffect ile yönetiyoruz
                DisposableEffect(Unit) {
                    val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        currentUser = firebaseAuth.currentUser
                        Log.d("xxMainActivity", "Auth state değişti: currentUser=${currentUser?.uid}, isEmailVerified=${currentUser?.isEmailVerified}")
                        when {
                            currentUser != null && currentUser!!.isEmailVerified -> {
                                Log.d("xxMainActivity", "Email doğrulanmış, ChatListScreen'e gidiliyor")
                                navController.navigate(AppScreens.ChatListScreen.name) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                            currentUser != null && !currentUser!!.isEmailVerified -> {
                                Log.d("xxMainActivity", "Email doğrulanmamış, bekleniyor...")
                            }
                            else -> {
                                Log.d("xxMainActivity", "Kullanıcı giriş yapmamış, LoginScreen'e gidiliyor")
                                navController.navigate(AppScreens.LoginScreen.name) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                        }
                    }

                    auth.addAuthStateListener(authListener)
                    onDispose {
                        auth.removeAuthStateListener(authListener)
                        Log.d("xxMainActivity", "Auth listener temizlendi")
                    }
                }

                // Başlangıç ekranı dinamik olarak belirleniyor
                val startDestination = if (currentUser != null && currentUser!!.isEmailVerified) {
                    AppScreens.ChatListScreen.name
                } else {
                    AppScreens.LoginScreen.name
                }

                AppNavigation(
                    startDestination = startDestination,
                    navController = navController
                )
            }
        }
    }
}