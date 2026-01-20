package com.skrainyukov.testmessenger.domain.usecase.auth

import com.skrainyukov.testmessenger.domain.repository.AuthRepository
import com.skrainyukov.testmessenger.util.Result
import javax.inject.Inject

class SendAuthCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        return authRepository.sendAuthCode(phone)
    }
}