package com.skrainyukov.testmessenger.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.skrainyukov.testmessenger.presentation.screens.auth.code.CodeScreen
import com.skrainyukov.testmessenger.presentation.screens.auth.phone.PhoneScreen
import com.skrainyukov.testmessenger.presentation.screens.auth.registration.RegistrationScreen
import com.skrainyukov.testmessenger.presentation.screens.chats.detail.ChatDetailScreen
import com.skrainyukov.testmessenger.presentation.screens.chats.list.ChatsListScreen
import com.skrainyukov.testmessenger.presentation.screens.profile.edit.EditProfileScreen
import com.skrainyukov.testmessenger.presentation.screens.profile.view.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Phone.route) {
            PhoneScreen(
                onNavigateToCode = { navController.navigate(Screen.Code.route) }
            )
        }

        composable(Screen.Code.route) {
            CodeScreen(
                onNavigateToRegistration = { navController.navigate(Screen.Registration.route) },
                onNavigateToChats = {
                    navController.navigate(Screen.ChatsList.route) {
                        popUpTo(Screen.Phone.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Registration.route) {
            RegistrationScreen(
                onNavigateToChats = {
                    navController.navigate(Screen.ChatsList.route) {
                        popUpTo(Screen.Phone.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ChatsList.route) {
            ChatsListScreen(
                onNavigateToChat = { chatId ->
                    navController.navigate(Screen.Chat.createRoute(chatId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAuth = {
                    navController.navigate(Screen.Phone.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}