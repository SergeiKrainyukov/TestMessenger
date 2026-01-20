package com.skrainyukov.testmessenger.presentation.screens.auth.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrainyukov.testmessenger.domain.repository.AuthRepository
import com.skrainyukov.testmessenger.domain.usecase.auth.SendAuthCodeUseCase
import com.skrainyukov.testmessenger.presentation.components.Country
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

    fun setInitialCountry(country: Country) {
        if (_state.value.selectedCountry == null) {
            _state.update { it.copy(selectedCountry = country) }
        }
    }

    fun onEvent(event: PhoneEvent) {
        when (event) {
            is PhoneEvent.OnPhoneNumberChanged -> onPhoneNumberChanged(event.phoneNumber)
            is PhoneEvent.OnCountrySelected -> onCountrySelected(event.country)
            PhoneEvent.OnSendCodeClick -> sendAuthCode()
        }
    }

    private fun onPhoneNumberChanged(phoneNumber: String) {
        _state.update {
            val fullPhone = (it.selectedCountry?.dialCode ?: "") + phoneNumber
            it.copy(
                phoneNumber = phoneNumber,
                isPhoneValid = isPhoneValid(phoneNumber),
                error = null
            )
        }
    }

    private fun onCountrySelected(country: Country) {
        _state.update { it.copy(
            selectedCountry = country,
            isPhoneValid = isPhoneValid(it.phoneNumber),
            error = null
        ) }
    }

    private fun sendAuthCode() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currentState = _state.value
            val fullPhone = (currentState.selectedCountry?.dialCode ?: "+7") + currentState.phoneNumber

            when (val result = sendAuthCodeUseCase(fullPhone)) {
                is Result.Success -> {
                    authRepository.savePhone(fullPhone)
                    _effect.send(PhoneEffect.NavigateToCode(fullPhone))
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

    private fun isPhoneValid(phoneNumber: String): Boolean {
        // Validate only the number part (without country code)
        // Should be 10 digits for most countries
        val digitsOnly = phoneNumber.filter { it.isDigit() }
        return digitsOnly.length in 10..11
    }
}