package com.skrainyukov.testmessenger.domain.usecase.profile

import com.skrainyukov.testmessenger.domain.model.User
import com.skrainyukov.testmessenger.domain.repository.UserRepository
import com.skrainyukov.testmessenger.util.Result
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        birthday: String?,
        city: String?,
        about: String?,
        avatarFilename: String?,
        avatarBase64: String?,
        shouldRemoveAvatar: Boolean = false
    ): Result<User> {
        return userRepository.updateUser(
            name = name,
            birthday = birthday,
            city = city,
            about = about,
            avatarFilename = avatarFilename,
            avatarBase64 = avatarBase64,
            shouldRemoveAvatar = shouldRemoveAvatar
        )
    }
}