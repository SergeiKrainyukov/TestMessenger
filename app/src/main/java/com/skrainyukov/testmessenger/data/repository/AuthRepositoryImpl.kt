package com.skrainyukov.testmessenger.data.repository

import com.skrainyukov.testmessenger.data.local.datastore.TokenDataStore
import com.skrainyukov.testmessenger.data.remote.api.AuthApi
import com.skrainyukov.testmessenger.data.remote.dto.CheckAuthCodeRequest
import com.skrainyukov.testmessenger.data.remote.dto.RegisterRequest
import com.skrainyukov.testmessenger.data.remote.dto.SendAuthCodeRequest
import com.skrainyukov.testmessenger.domain.model.AuthResult
import com.skrainyukov.testmessenger.domain.repository.AuthRepository
import com.skrainyukov.testmessenger.util.Result
import com.skrainyukov.testmessenger.util.runCatchingResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore
) : AuthRepository {

    override suspend fun sendAuthCode(phone: String): Result<Unit> {
        return runCatchingResult {
            authApi.sendAuthCode(SendAuthCodeRequest(phone))
        }
    }

    override suspend fun checkAuthCode(phone: String, code: String): Result<AuthResult> {
        return runCatchingResult {
            val response = authApi.checkAuthCode(CheckAuthCodeRequest(phone, code))

            // Save tokens
            tokenDataStore.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.userId
            )

            AuthResult(
                refreshToken = response.refreshToken,
                accessToken = response.accessToken,
                userId = response.userId,
                isUserExists = response.isUserExists
            )
        }
    }

    override suspend fun register(phone: String, name: String, username: String): Result<AuthResult> {
        return runCatchingResult {
            val response = authApi.register(RegisterRequest(phone, name, username))

            // Save tokens
            tokenDataStore.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.userId
            )

            AuthResult(
                refreshToken = response.refreshToken,
                accessToken = response.accessToken,
                userId = response.userId,
                isUserExists = true
            )
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return tokenDataStore.accessToken.first() != null
    }

    override suspend fun logout() {
        tokenDataStore.clearTokens()
    }

    override suspend fun getPhone(): String? {
        return tokenDataStore.phone.first()
    }

    override suspend fun savePhone(phone: String) {
        tokenDataStore.savePhone(phone)
    }
}