package com.mehmetbaloglu.mychatapp.widgets

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.mehmetbaloglu.mychatapp.R
import com.mehmetbaloglu.mychatapp.navigation.AppScreens

object AppWidgets {

    @Composable
    fun BottomNavigationBar(navController: NavHostController, currentRoute: String) {
        Log.d("xxBottomNavBar", "Rendering BottomNavigationBar, currentRoute: $currentRoute")

        NavigationBar {
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Sohbetler") },
                selected = currentRoute == AppScreens.ChatListScreen.name,
                onClick = {
                    if (currentRoute != AppScreens.ChatListScreen.name) {
                        Log.d("xxBottomNavBar", "Navigating to ChatListScreen")
                        navController.navigate(AppScreens.ChatListScreen.name) {
                            popUpTo(AppScreens.ChatListScreen.name) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile") },
                label = { Text("Profil") },
                selected = currentRoute == AppScreens.ProfileScreen.name,
                onClick = {
                    if (currentRoute != AppScreens.ProfileScreen.name) {
                        Log.d("xxBottomNavBar", "Navigating to ProfileScreen")
                        navController.navigate(AppScreens.ProfileScreen.name) {
                            popUpTo(AppScreens.ChatListScreen.name) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}