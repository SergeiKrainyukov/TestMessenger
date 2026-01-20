package com.skrainyukov.testmessenger.domain.repository

import com.skrainyukov.testmessenger.domain.model.AuthResult
import com.skrainyukov.testmessenger.util.Result

interface AuthRepository {
    suspend fun sendAuthCode(phone: String): Result<Unit>
    suspend fun checkAuthCode(phone: String, code: String): Result<AuthResult>
    suspend fun register(phone: String, name: String, username: String): Result<AuthResult>
    suspend fun isAuthenticated(): Boolean
    suspend fun logout()
    suspend fun getPhone(): String?
    suspend fun savePhone(phone: String)
}