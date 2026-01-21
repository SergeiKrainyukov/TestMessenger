package com.skrainyukov.testmessenger.presentation.screens.profile.edit

import android.net.Uri

data class EditProfileState(
    val name: String = "",
    val birthday: String = "",
    val city: String = "",
    val about: String = "",
    val currentAvatarUrl: String? = null,
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInitialLoading: Boolean = true
)

sealed class EditProfileEvent {
    data class OnNameChanged(val name: String) : EditProfileEvent()
    data class OnBirthdayChanged(val birthday: String) : EditProfileEvent()
    data class OnCityChanged(val city: String) : EditProfileEvent()
    data class OnAboutChanged(val about: String) : EditProfileEvent()
    data class OnImageSelected(val uri: Uri) : EditProfileEvent()
    data object OnRemoveImage : EditProfileEvent()
    data object OnSaveClick : EditProfileEvent()
}

sealed class EditProfileEffect {
    data object NavigateBack : EditProfileEffect()
    data object NavigateToAuth : EditProfileEffect()
    data class ShowError(val message: String) : EditProfileEffect()
    data object ShowSuccess : EditProfileEffect()
}