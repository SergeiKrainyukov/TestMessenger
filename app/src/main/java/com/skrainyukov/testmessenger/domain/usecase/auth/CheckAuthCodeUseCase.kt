package com.skrainyukov.testmessenger.domain.usecase.auth

import com.skrainyukov.testmessenger.domain.model.AuthResult
import com.skrainyukov.testmessenger.domain.repository.AuthRepository
import com.skrainyukov.testmessenger.util.Result
import javax.inject.Inject

class CheckAuthCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String, code: String): Result<AuthResult> {
        return authRepository.checkAuthCode(phone, code)
    }
}