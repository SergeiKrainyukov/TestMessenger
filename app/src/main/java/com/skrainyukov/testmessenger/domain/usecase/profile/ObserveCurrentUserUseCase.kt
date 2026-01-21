package com.skrainyukov.testmessenger.domain.usecase.profile

import com.skrainyukov.testmessenger.domain.model.User
import com.skrainyukov.testmessenger.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<User?> {
        return userRepository.observeCurrentUser()
    }
}
