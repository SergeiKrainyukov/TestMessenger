package com.skrainyukov.testmessenger.presentation.screens.auth.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrainyukov.testmessenger.domain.repository.AuthRepository
import com.skrainyukov.testmessenger.domain.usecase.auth.SendAuthCodeUseCase
import com.skrainyukov.testmessenger.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneViewModel @Inject constructor(
    private val sendAuthCodeUseCase: SendAuthCodeUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PhoneState())
    val state: StateFlow<PhoneState> = _state.asStateFlow()

    private val _effect = Channel<PhoneEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: PhoneEvent) {
        when (event) {
            is PhoneEvent.OnPhoneChanged -> onPhoneChanged(event.phone)
            PhoneEvent.OnSendCodeClick -> sendAuthCode()
        }
    }

    private fun onPhoneChanged(phone: String) {
        _state.update { it.copy(
            phone = phone,
            isPhoneValid = isPhoneValid(phone),
            error = null
        ) }
    }

    private fun sendAuthCode() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val phone = _state.value.phone
            when (val result = sendAuthCodeUseCase(phone)) {
                is Result.Success -> {
                    authRepository.savePhone(phone)
                    _effect.send(PhoneEffect.NavigateToCode(phone))
                }
                is Result.Error -> {
                    _state.update { it.copy(
                        error = result.message ?: "Ошибка отправки кода"
                    ) }
                    _effect.send(PhoneEffect.ShowError(result.message ?: "Ошибка отправки кода"))
                }
                is Result.Loading -> {}
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun isPhoneValid(phone: String): Boolean {
        // Basic validation: phone should start with + and have 11-15 digits
        val digitsOnly = phone.filter { it.isDigit() }
        return phone.startsWith("+") && digitsOnly.length in 10..15
    }
}