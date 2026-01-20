package com.skrainyukov.testmessenger.data.repository

import com.skrainyukov.testmessenger.data.local.dao.UserDao
import com.skrainyukov.testmessenger.data.local.datastore.TokenDataStore
import com.skrainyukov.testmessenger.data.local.entity.toEntity
import com.skrainyukov.testmessenger.data.remote.api.UserApi
import com.skrainyukov.testmessenger.data.local.entity.toDomain
import com.skrainyukov.testmessenger.data.remote.dto.AvatarData
import com.skrainyukov.testmessenger.data.remote.dto.UpdateUserRequest
import com.skrainyukov.testmessenger.data.remote.dto.toDomain
import com.skrainyukov.testmessenger.domain.model.User
import com.skrainyukov.testmessenger.domain.repository.UserRepository
import com.skrainyukov.testmessenger.util.Result
import com.skrainyukov.testmessenger.util.runCatchingResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val tokenDataStore: TokenDataStore
) : UserRepository {

    override suspend fun getCurrentUser(forceRefresh: Boolean): Result<User> {
        return runCatchingResult {
            val userId = tokenDataStore.userId.first()
                ?: throw IllegalStateException("User not authenticated")

            // Try to get from cache first
            if (!forceRefresh) {
                val cachedUser = userDao.getUserById(userId).first()
                if (cachedUser != null) {
                    return@runCatchingResult cachedUser.toDomain()
                }
            }

            // Fetch from API
            val response = userApi.getCurrentUser()
            val user = response.profileData.toDomain()

            // Save to cache
            userDao.insertUser(user.toEntity())

            user
        }
    }

    override fun observeCurrentUser(): Flow<User?> {
        val userId = tokenDataStore.userId
        return userId.map { id ->
            id?.let { userDao.getUserById(it).first()?.toDomain() }
        }
    }

    override suspend fun updateUser(
        name: String,
        birthday: String?,
        city: String?,
        about: String?,
        avatarFilename: String?,
        avatarBase64: String?
    ): Result<User> {
        return runCatchingResult {
            val avatarData = if (avatarFilename != null && avatarBase64 != null) {
                AvatarData(avatarFilename, avatarBase64)
            } else null

            val response = userApi.updateUser(
                UpdateUserRequest(
                    name = name,
                    birthday = birthday,
                    city = city,
                    status = about,
                    avatar = avatarData
                )
            )

            val user = response.profileData.toDomain()

            // Update cache
            userDao.insertUser(user.toEntity())

            user
        }
    }
}