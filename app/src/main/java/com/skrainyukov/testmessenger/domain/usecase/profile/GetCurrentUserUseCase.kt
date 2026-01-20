package com.skrainyukov.testmessenger.domain.usecase.profile

import com.skrainyukov.testmessenger.domain.model.User
import com.skrainyukov.testmessenger.domain.repository.UserRepository
import com.skrainyukov.testmessenger.util.Result
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<User> {
        return userRepository.getCurrentUser(forceRefresh)
    }
}