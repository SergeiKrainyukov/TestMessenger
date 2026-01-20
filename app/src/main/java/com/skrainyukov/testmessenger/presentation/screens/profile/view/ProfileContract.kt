package com.skrainyukov.testmessenger.presentation.screens.profile.view

import com.skrainyukov.testmessenger.domain.model.User

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ProfileEvent {
    data object OnRefresh : ProfileEvent()
    data object OnEditClick : ProfileEvent()
    data object OnLogoutClick : ProfileEvent()
}

sealed class ProfileEffect {
    data object NavigateToEdit : ProfileEffect()
    data object NavigateToAuth : ProfileEffect()
    data class ShowError(val message: String) : ProfileEffect()
}