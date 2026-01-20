package com.skrainyukov.testmessenger.presentation.screens.auth.code

data class CodeState(
    val code: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class CodeEvent {
    data class OnCodeChanged(val code: String) : CodeEvent()
    data object OnVerifyClick : CodeEvent()
}

sealed class CodeEffect {
    data object NavigateToRegistration : CodeEffect()
    data object NavigateToChats : CodeEffect()
    data class ShowError(val message: String) : CodeEffect()
}