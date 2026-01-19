package com.skrainyukov.testmessenger.domain.model

data class AuthResult(
    val refreshToken: String,
    val accessToken: String,
    val userId: Long,
    val isUserExists: Boolean
)