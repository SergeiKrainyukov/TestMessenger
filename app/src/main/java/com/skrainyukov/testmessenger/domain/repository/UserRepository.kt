package com.skrainyukov.testmessenger.domain.repository

import com.skrainyukov.testmessenger.domain.model.User
import com.skrainyukov.testmessenger.util.Result
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getCurrentUser(forceRefresh: Boolean = false): Result<User>
    fun observeCurrentUser(): Flow<User?>
    suspend fun updateUser(
        name: String,
        birthday: String?,
        city: String?,
        about: String?,
        avatarFilename: String?,
        avatarBase64: String?
    ): Result<User>
}