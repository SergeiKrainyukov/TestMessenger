package com.skrainyukov.testmessenger.presentation.screens.auth.code

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrainyukov.testmessenger.domain.repository.AuthRepository
import com.skrainyukov.testmessenger.domain.usecase.auth.CheckAuthCodeUseCase
import com.skrainyukov.testmessenger.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CodeViewModel @Inject constructor(
    private val checkAuthCodeUseCase: CheckAuthCodeUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CodeState())
    val state: StateFlow<CodeState> = _state.asStateFlow()

    private val _effect = Channel<CodeEffect>()
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

    fun onEvent(event: CodeEvent) {
        when (event) {
            is CodeEvent.OnCodeChanged -> _state.update { it.copy(code = event.code, error = null) }
            CodeEvent.OnVerifyClick -> verifyCode()
        }
    }

    private fun verifyCode() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val phone = _state.value.phone
            val code = _state.value.code

            when (val result = checkAuthCodeUseCase(phone, code)) {
                is Result.Success -> {
                    if (result.data.isUserExists) {
                        _effect.send(CodeEffect.NavigateToChats)
                    } else {
                        _effect.send(CodeEffect.NavigateToRegistration)
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(error = result.message ?: "Ошибка проверки кода") }
                    _effect.send(CodeEffect.ShowError(result.message ?: "Ошибка проверки кода"))
                }
                is Result.Loading -> {}
            }

            _state.update { it.copy(isLoading = false) }
        }
    }
}