package com.skrainyukov.testmessenger.data.remote.dto

import com.skrainyukov.testmessenger.domain.model.User
import com.skrainyukov.testmessenger.domain.model.ZodiacSign
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id") val id: Long,
    @SerialName("phone") val phone: String,
    @SerialName("username") val username: String,
    @SerialName("name") val name: String,
    @SerialName("birthday") val birthday: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("avatar") val avatar: String? = null,
    @SerialName("status") val about: String? = null
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        phone = phone,
        username = username,
        name = name,
        birthday = birthday,
        city = city,
        avatar = avatar,
        about = about,
        zodiacSign = ZodiacSign.fromDate(birthday)
    )
}

@Serializable
data class ProfileData(
    @SerialName("profile_data") val profileData: UserDto
)

@Serializable
data class UpdateUserRequest(
    @SerialName("name") val name: String,
    @SerialName("birthday") val birthday: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("avatar") val avatar: AvatarData? = null
)

@Serializable
data class AvatarData(
    @SerialName("filename") val filename: String,
    @SerialName("base_64") val base64: String
)