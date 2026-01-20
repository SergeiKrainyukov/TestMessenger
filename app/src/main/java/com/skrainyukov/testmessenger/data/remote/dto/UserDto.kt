package com.skrainyukov.testmessenger.data.remote.dto

import com.skrainyukov.testmessenger.domain.model.User
import com.skrainyukov.testmessenger.domain.model.ZodiacSign
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Avatars(
    @SerialName("avatar") val avatar: String? = null,
    @SerialName("bigAvatar") val bigAvatar: String,
    @SerialName("miniAvatar") val miniAvatar: String
)

@Serializable
data class UserDto(
    @SerialName("id") val id: Long,
    @SerialName("phone") val phone: String,
    @SerialName("username") val username: String,
    @SerialName("name") val name: String,
    @SerialName("birthday") val birthday: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("vk") val vk: String? = null,
    @SerialName("instagram") val instagram: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("avatar") val avatar: String? = null,
    @SerialName("online") val online: Boolean,
    @SerialName("last") val last: String? = null,
    @SerialName("created") val created: String? = null,
    @SerialName("completed_task") val completedTask: Int = 0,
    @SerialName("avatars") val avatars: Avatars? = null
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
        about = status,
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
    @SerialName("username") val username: String,
    @SerialName("birthday") val birthday: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("vk") val vk: String? = null,
    @SerialName("instagram") val instagram: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("avatar") val avatar: AvatarData? = null
)

@Serializable
data class UpdateUserResponse(
    @SerialName("avatars") val avatars: Avatars? = null
)

@Serializable
data class AvatarData(
    @SerialName("filename") val filename: String,
    @SerialName("base_64") val base64: String
)