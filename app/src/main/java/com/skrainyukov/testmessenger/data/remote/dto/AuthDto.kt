package com.skrainyukov.testmessenger.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendAuthCodeRequest(
    @SerialName("phone") val phone: String
)

@Serializable
data class SendAuthCodeResponse(
    @SerialName("is_success") val isSuccess: Boolean
)

@Serializable
data class CheckAuthCodeRequest(
    @SerialName("phone") val phone: String,
    @SerialName("code") val code: String
)

@Serializable
data class CheckAuthCodeResponse(
    @SerialName("refresh_token") val refreshToken: String?,
    @SerialName("access_token") val accessToken: String?,
    @SerialName("user_id") val userId: Long?,
    @SerialName("is_user_exists") val isUserExists: Boolean
)

@Serializable
data class RegisterRequest(
    @SerialName("phone") val phone: String,
    @SerialName("name") val name: String,
    @SerialName("username") val username: String
)

@Serializable
data class RegisterResponse(
    @SerialName("refresh_token") val refreshToken: String?,
    @SerialName("access_token") val accessToken: String?,
    @SerialName("user_id") val userId: Long?
)
