package com.skrainyukov.testmessenger.presentation.screens.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrainyukov.testmessenger.domain.usecase.profile.GetCurrentUserUseCase
import com.skrainyukov.testmessenger.domain.usecase.profile.UpdateUserUseCase
import com.skrainyukov.testmessenger.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
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
                            isInitialLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isInitialLoading = false) }
                    _effect.send(EditProfileEffect.ShowError(result.message ?: "Ошибка загрузки"))
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
            EditProfileEvent.OnSaveClick -> saveProfile()
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = updateUserUseCase(
                name = _state.value.name,
                birthday = _state.value.birthday.takeIf { it.isNotBlank() },
                city = _state.value.city.takeIf { it.isNotBlank() },
                about = _state.value.about.takeIf { it.isNotBlank() },
                avatarFilename = null,
                avatarBase64 = null
            )

            when (result) {
                is Result.Success -> {
                    _effect.send(EditProfileEffect.ShowSuccess)
                    _effect.send(EditProfileEffect.NavigateBack)
                }
                is Result.Error -> {
                    _state.update { it.copy(error = result.message ?: "Ошибка сохранения") }
                    _effect.send(EditProfileEffect.ShowError(result.message ?: "Ошибка сохранения"))
                }
                is Result.Loading -> {}
            }

            _state.update { it.copy(isLoading = false) }
        }
    }
}