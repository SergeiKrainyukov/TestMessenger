package com.skrainyukov.testmessenger.presentation.screens.auth.registration

data class RegistrationState(
    val phone: String = "",
    val name: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFormValid: Boolean = false
)

sealed class RegistrationEvent {
    data class OnNameChanged(val name: String) : RegistrationEvent()
    data class OnUsernameChanged(val username: String) : RegistrationEvent()
    data object OnRegisterClick : RegistrationEvent()
}

sealed class RegistrationEffect {
    data object NavigateToChats : RegistrationEffect()
    data class ShowError(val message: String) : RegistrationEffect()
}