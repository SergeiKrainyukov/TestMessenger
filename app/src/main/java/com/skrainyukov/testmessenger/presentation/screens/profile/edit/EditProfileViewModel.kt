package com.skrainyukov.testmessenger.presentation.screens.profile.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrainyukov.testmessenger.domain.usecase.profile.GetCurrentUserUseCase
import com.skrainyukov.testmessenger.domain.usecase.profile.UpdateUserUseCase
import com.skrainyukov.testmessenger.util.AuthException
import com.skrainyukov.testmessenger.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    private val _effect = Channel<EditProfileEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            name = result.data.name,
                            birthday = result.data.birthday ?: "",
                            city = result.data.city ?: "",
                            about = result.data.about ?: "",
                            currentAvatarUrl = result.data.avatar,
                            isInitialLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isInitialLoading = false) }

                    if (result.exception is AuthException) {
                        _effect.send(EditProfileEffect.NavigateToAuth)
                    } else {
                        _effect.send(EditProfileEffect.ShowError(result.message ?: "Ошибка загрузки"))
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.OnNameChanged -> {
                _state.update { it.copy(name = event.name, error = null) }
            }
            is EditProfileEvent.OnBirthdayChanged -> {
                _state.update { it.copy(birthday = event.birthday, error = null) }
            }
            is EditProfileEvent.OnCityChanged -> {
                _state.update { it.copy(city = event.city, error = null) }
            }
            is EditProfileEvent.OnAboutChanged -> {
                _state.update { it.copy(about = event.about, error = null) }
            }
            is EditProfileEvent.OnImageSelected -> {
                _state.update { it.copy(selectedImageUri = event.uri, shouldRemoveAvatar = false, error = null) }
            }
            EditProfileEvent.OnRemoveImage -> {
                _state.update { it.copy(selectedImageUri = null, shouldRemoveAvatar = true, error = null) }
            }
            EditProfileEvent.OnSaveClick -> saveProfile()
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Process image if selected
            val imageData = _state.value.selectedImageUri?.let { uri ->
                try {
                    uriToBase64(uri)
                } catch (e: Exception) {
                    _effect.send(EditProfileEffect.ShowError("Ошибка обработки изображения"))
                    null
                }
            }

            val result = updateUserUseCase(
                name = _state.value.name,
                birthday = _state.value.birthday.takeIf { it.isNotBlank() },
                city = _state.value.city.takeIf { it.isNotBlank() },
                about = _state.value.about.takeIf { it.isNotBlank() },
                avatarFilename = imageData?.first,
                avatarBase64 = imageData?.second,
                shouldRemoveAvatar = _state.value.shouldRemoveAvatar
            )

            when (result) {
                is Result.Success -> {
                    _effect.send(EditProfileEffect.ShowSuccess)
                    _effect.send(EditProfileEffect.NavigateBack)
                }
                is Result.Error -> {
                    if (result.exception is AuthException) {
                        _effect.send(EditProfileEffect.NavigateToAuth)
                    } else {
                        _state.update { it.copy(error = result.message ?: "Ошибка сохранения") }
                        _effect.send(EditProfileEffect.ShowError(result.message ?: "Ошибка сохранения"))
                    }
                }
                is Result.Loading -> {}
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun uriToBase64(uri: Uri): Pair<String, String> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open image")

        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val maxSize = 1024
        val ratio = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
        val resizedBitmap = if (ratio < 1) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        val filename = "avatar_${System.currentTimeMillis()}.jpg"

        return Pair(filename, base64)
    }
}