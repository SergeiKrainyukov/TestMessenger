package com.skrainyukov.testmessenger.presentation.screens.profile.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrainyukov.testmessenger.domain.repository.AuthRepository
import com.skrainyukov.testmessenger.domain.usecase.profile.GetCurrentUserUseCase
import com.skrainyukov.testmessenger.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = Channel<ProfileEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnRefresh -> loadProfile(forceRefresh = true)
            ProfileEvent.OnEditClick -> viewModelScope.launch {
                _effect.send(ProfileEffect.NavigateToEdit)
            }
            ProfileEvent.OnLogoutClick -> logout()
        }
    }

    private fun loadProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = getCurrentUserUseCase(forceRefresh)) {
                is Result.Success -> {
                    _state.update { it.copy(user = result.data, isLoading = false) }
                }
                is Result.Error -> {
                    _state.update { it.copy(
                        error = result.message ?: "Ошибка загрузки профиля",
                        isLoading = false
                    ) }
                    _effect.send(ProfileEffect.ShowError(result.message ?: "Ошибка загрузки профиля"))
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _effect.send(ProfileEffect.NavigateToAuth)
        }
    }
}