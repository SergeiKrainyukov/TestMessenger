package com.skrainyukov.testmessenger.data.remote.api

import com.skrainyukov.testmessenger.data.remote.dto.ProfileData
import com.skrainyukov.testmessenger.data.remote.dto.UpdateUserRequest
import com.skrainyukov.testmessenger.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {
    @GET("/api/v1/users/me/")
    suspend fun getCurrentUser(): ProfileData

    @PUT("/api/v1/users/me/")
    suspend fun updateUser(@Body request: UpdateUserRequest): ProfileData
}