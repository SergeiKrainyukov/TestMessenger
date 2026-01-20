package com.skrainyukov.testmessenger.presentation.screens.auth.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrainyukov.testmessenger.domain.repository.AuthRepository
import com.skrainyukov.testmessenger.domain.usecase.auth.RegisterUseCase
import com.skrainyukov.testmessenger.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    private val _effect = Channel<RegistrationEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadPhone()
    }

    private fun loadPhone() {
        viewModelScope.launch {
            val phone = authRepository.getPhone() ?: ""
            _state.update { it.copy(phone = phone) }
        }
    }

    fun onEvent(event: RegistrationEvent) {
        when (event) {
            is RegistrationEvent.OnNameChanged -> {
                _state.update {
                    it.copy(
                        name = event.name,
                        error = null,
                        isFormValid = event.name.isNotBlank() && isUsernameValid(it.username)
                    )
                }
            }
            is RegistrationEvent.OnUsernameChanged -> {
                val filtered = event.username.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
                _state.update {
                    it.copy(
                        username = filtered,
                        error = null,
                        isFormValid = filtered.isNotBlank() && it.name.isNotBlank()
                    )
                }
            }
            RegistrationEvent.OnRegisterClick -> register()
        }
    }

    private fun register() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val phone = _state.value.phone
            val name = _state.value.name
            val username = _state.value.username

            when (val result = registerUseCase(phone, name, username)) {
                is Result.Success -> {
                    _effect.send(RegistrationEffect.NavigateToChats)
                }
                is Result.Error -> {
                    _state.update { it.copy(error = result.message ?: "Ошибка регистрации") }
                    _effect.send(RegistrationEffect.ShowError(result.message ?: "Ошибка регистрации"))
                }
                is Result.Loading -> {}
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun isUsernameValid(username: String): Boolean {
        return username.isNotBlank() && username.all {
            it.isLetterOrDigit() || it == '-' || it == '_'
        }
    }
}