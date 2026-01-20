package com.skrainyukov.testmessenger.domain.usecase.auth

import com.skrainyukov.testmessenger.domain.model.AuthResult
import com.skrainyukov.testmessenger.domain.repository.AuthRepository
import com.skrainyukov.testmessenger.util.Result
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String, name: String, username: String): Result<AuthResult> {
        return authRepository.register(phone, name, username)
    }
}