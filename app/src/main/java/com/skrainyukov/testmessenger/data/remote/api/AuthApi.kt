package com.skrainyukov.testmessenger.data.remote.api

import com.skrainyukov.testmessenger.data.remote.HttpHeaders
import com.skrainyukov.testmessenger.data.remote.dto.CheckAuthCodeRequest
import com.skrainyukov.testmessenger.data.remote.dto.CheckAuthCodeResponse
import com.skrainyukov.testmessenger.data.remote.dto.RegisterRequest
import com.skrainyukov.testmessenger.data.remote.dto.RegisterResponse
import com.skrainyukov.testmessenger.data.remote.dto.SendAuthCodeRequest
import com.skrainyukov.testmessenger.data.remote.dto.SendAuthCodeResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @Headers(HttpHeaders.NO_AUTHENTICATION_HEADER)
    @POST("/api/v1/users/send-auth-code/")
    suspend fun sendAuthCode(@Body request: SendAuthCodeRequest): SendAuthCodeResponse

    @Headers(HttpHeaders.NO_AUTHENTICATION_HEADER)
    @POST("/api/v1/users/check-auth-code/")
    suspend fun checkAuthCode(@Body request: CheckAuthCodeRequest): CheckAuthCodeResponse

    @Headers(HttpHeaders.NO_AUTHENTICATION_HEADER)
    @POST("/api/v1/users/register/")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
}