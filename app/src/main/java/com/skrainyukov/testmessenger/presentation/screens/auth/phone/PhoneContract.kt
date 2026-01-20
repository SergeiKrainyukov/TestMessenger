package com.skrainyukov.testmessenger.presentation.screens.auth.phone

// MVI Contract Pattern

data class PhoneState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPhoneValid: Boolean = false
)

sealed class PhoneEvent {
    data class OnPhoneChanged(val phone: String) : PhoneEvent()
    data object OnSendCodeClick : PhoneEvent()
}

sealed class PhoneEffect {
    data class NavigateToCode(val phone: String) : PhoneEffect()
    data class ShowError(val message: String) : PhoneEffect()
}