package com.mehmetbaloglu.mychatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mehmetbaloglu.mychatapp.navigation.AppNavigation
import com.mehmetbaloglu.mychatapp.navigation.AppScreens
import com.mehmetbaloglu.mychatapp.ui.theme.MyChatAppTheme
import com.mehmetbaloglu.mychatapp.widgets.AppWidgets
import com.mehmetbaloglu.mychatapp.widgets.AppWidgets.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyChatAppTheme {
                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                Log.d("xxMainActivity", "Current Route: $currentRoute")

                // Firebase Authentication ile başlangıç ekranını kontrol et
                val currentUser = FirebaseAuth.getInstance().currentUser
                val startDestination = if (currentUser != null) {
                    AppScreens.ChatListScreen.name
                } else {
                    AppScreens.LoginScreen.name
                }

                Scaffold (
                    bottomBar = {
                        // Koşulu sadeleştirip log ekleyelim
                        if (currentUser != null) {
                            Log.d("xxMainActivity", "Rendering BottomNavigationBar")
                            BottomNavigationBar(navController = navController)
                        } else {

                        }
                    }
                ){ innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding), startDestination = startDestination)
                }
            }
        }
    }
}

