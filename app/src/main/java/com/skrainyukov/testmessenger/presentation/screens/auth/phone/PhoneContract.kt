package com.skrainyukov.testmessenger.presentation.screens.auth.phone

import com.skrainyukov.testmessenger.presentation.components.Country

// MVI Contract Pattern

data class PhoneState(
    val phoneNumber: String = "",
    val selectedCountry: Country? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPhoneValid: Boolean = false
)

sealed class PhoneEvent {
    data class OnPhoneNumberChanged(val phoneNumber: String) : PhoneEvent()
    data class OnCountrySelected(val country: Country) : PhoneEvent()
    data object OnSendCodeClick : PhoneEvent()
}

sealed class PhoneEffect {
    data class NavigateToCode(val phone: String) : PhoneEffect()
    data class ShowError(val message: String) : PhoneEffect()
}