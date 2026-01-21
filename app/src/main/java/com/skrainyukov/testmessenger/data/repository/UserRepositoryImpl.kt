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
import com.skrainyukov.testmessenger.util.AuthException
import com.skrainyukov.testmessenger.util.Result
import com.skrainyukov.testmessenger.util.runCatchingResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val tokenDataStore: TokenDataStore
) : UserRepository {

    companion object {
        private const val USER_NOT_AUTHENTICATED_MESSAGE = "User not authenticated"
        private const val USER_NOT_FOUND_MESSAGE = "User not found in cache"
    }

    override suspend fun getCurrentUser(forceRefresh: Boolean): Result<User> {
        return runCatchingResult {
            val userId = tokenDataStore.userId.first()
                ?: throw AuthException(USER_NOT_AUTHENTICATED_MESSAGE)

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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCurrentUser(): Flow<User?> {
        return tokenDataStore.userId.flatMapLatest { id ->
            if (id != null) {
                userDao.getUserById(id).map { it?.toDomain() }
            } else {
                flowOf(null)
            }
        }
    }

    override suspend fun updateUser(
        name: String,
        birthday: String?,
        city: String?,
        about: String?,
        avatarFilename: String?,
        avatarBase64: String?,
        shouldRemoveAvatar: Boolean
    ): Result<User> {
        return runCatchingResult {
            val userId = tokenDataStore.userId.first()
                ?: throw AuthException(USER_NOT_AUTHENTICATED_MESSAGE)

            val cachedUser = userDao.getUserById(userId).first()
                ?: throw IllegalStateException(USER_NOT_FOUND_MESSAGE)

            val avatarData = when {
                shouldRemoveAvatar -> AvatarData("", "")  // Send empty strings to remove avatar
                avatarFilename != null && avatarBase64 != null -> AvatarData(avatarFilename, avatarBase64)
                else -> null
            }

            // Update user - returns only avatars
            userApi.updateUser(
                UpdateUserRequest(
                    name = name,
                    username = cachedUser.username,
                    birthday = birthday,
                    city = city,
                    vk = null,
                    instagram = null,
                    status = about,
                    avatar = avatarData
                )
            )

            // Get updated user data
            val response = userApi.getCurrentUser()
            val user = response.profileData.toDomain()

            // Update cache
            userDao.insertUser(user.toEntity())

            user
        }
    }
}