package com.mehmetbaloglu.mychatapp.widgets

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
    fun BottomNavigationBar(navController: NavHostController) {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Sohbetler") },
                selected = navController.currentDestination?.route == AppScreens.ChatListScreen.name,
                onClick = {
                    navController.navigate(AppScreens.ChatListScreen.name) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile") },
                label = { Text("Profil") },
                selected = navController.currentDestination?.route == AppScreens.ProfileScreen.name,
                onClick = {
                    navController.navigate(AppScreens.ProfileScreen.name) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }

}