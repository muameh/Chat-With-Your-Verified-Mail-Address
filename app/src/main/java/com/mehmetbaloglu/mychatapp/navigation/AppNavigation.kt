package com.mehmetbaloglu.mychatapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mehmetbaloglu.mychatapp.ui.screens.ChatListScreen
import com.mehmetbaloglu.mychatapp.ui.screens.ChatScreen
import com.mehmetbaloglu.mychatapp.ui.screens.FriendRequestsDetailScreen
import com.mehmetbaloglu.mychatapp.ui.screens.FriendsListScreen
import com.mehmetbaloglu.mychatapp.ui.screens.LogInScreen
import com.mehmetbaloglu.mychatapp.ui.screens.ProfileScreen
import com.mehmetbaloglu.mychatapp.ui.screens.RegisterScreen
import com.mehmetbaloglu.mychatapp.ui.screens.SearchOtherUsersScreen
import com.mehmetbaloglu.mychatapp.ui.screens.SettingsScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    startDestination:String = AppScreens.LoginScreen.name) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(AppScreens.RegisterScreen.name) {
            RegisterScreen(navController = navController)
        }

        composable(AppScreens.ChatListScreen.name) {
            ChatListScreen(navController = navController)
        }

        composable(AppScreens.LoginScreen.name) {
            LogInScreen(navController)
        }

        composable("${AppScreens.ChatScreen.name}/{friendId}") { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            ChatScreen(navController = navController, friendId = friendId)
        }
        composable(AppScreens.ProfileScreen.name) {
            ProfileScreen(navController = navController)
        }
        composable(AppScreens.SearchOtherUsersScreen.name) {
            SearchOtherUsersScreen(navController)
        }
        composable(AppScreens.SettingsScreen.name) {
            SettingsScreen(navController = navController)
        }
        composable(AppScreens.FriendsListScreen.name) {
            FriendsListScreen(navController = navController)
        }
        composable("${AppScreens.FriendRequestsDetailScreen.name}/{requestId}") { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
            FriendRequestsDetailScreen(navController = navController, requestId = requestId)
        }


    }
}