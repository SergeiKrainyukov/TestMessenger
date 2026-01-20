package com.skrainyukov.testmessenger.data.remote.api

import com.skrainyukov.testmessenger.data.remote.dto.CheckAuthCodeRequest
import com.skrainyukov.testmessenger.data.remote.dto.CheckAuthCodeResponse
import com.skrainyukov.testmessenger.data.remote.dto.RefreshTokenRequest
import com.skrainyukov.testmessenger.data.remote.dto.RefreshTokenResponse
import com.skrainyukov.testmessenger.data.remote.dto.RegisterRequest
import com.skrainyukov.testmessenger.data.remote.dto.RegisterResponse
import com.skrainyukov.testmessenger.data.remote.dto.SendAuthCodeRequest
import com.skrainyukov.testmessenger.data.remote.dto.SendAuthCodeResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/v1/users/send-auth-code/")
    suspend fun sendAuthCode(@Body request: SendAuthCodeRequest): SendAuthCodeResponse

    @POST("/api/v1/users/check-auth-code/")
    suspend fun checkAuthCode(@Body request: CheckAuthCodeRequest): CheckAuthCodeResponse

    @POST("/api/v1/users/register/")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("/api/v1/users/refresh-token/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse
}