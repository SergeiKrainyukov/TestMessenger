package com.skrainyukov.testmessenger.presentation.navigation

sealed class Screen(val route: String) {
    data object Phone : Screen("phone")
    data object Code : Screen("code")
    data object Registration : Screen("registration")
    data object ChatsList : Screen("chats_list")
    data object Chat : Screen("chat/{chatId}") {
        fun createRoute(chatId: Long) = "chat/$chatId"
    }
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
}