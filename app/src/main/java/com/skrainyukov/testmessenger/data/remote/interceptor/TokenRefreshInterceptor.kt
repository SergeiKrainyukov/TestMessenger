package com.skrainyukov.testmessenger.data.remote.interceptor

import com.skrainyukov.testmessenger.data.local.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {

    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    // Separate OkHttpClient for refresh requests to avoid circular dependency
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // If 401 and not a refresh/auth endpoint, try to refresh token
        if (response.code == 401 && !request.url.toString().contains("/refresh-token/")) {
            response.close()

            val newResponse = runBlocking {
                mutex.withLock {
                    val refreshToken = tokenDataStore.refreshToken.first()

                    if (refreshToken != null) {
                        try {
                            // Make refresh request using separate client
                            val refreshRequestBody = """{"refresh_token":"$refreshToken"}"""
                                .toRequestBody("application/json".toMediaType())

                            val refreshRequest = Request.Builder()
                                .url("https://plannerok.ru/api/v1/users/refresh-token/")
                                .post(refreshRequestBody)
                                .build()

                            val refreshResponse = refreshClient.newCall(refreshRequest).execute()

                            if (refreshResponse.isSuccessful) {
                                val responseBody = refreshResponse.body?.string() ?: ""

                                if (responseBody.isNotEmpty()) {
                                    val jsonResponse = json.parseToJsonElement(responseBody).jsonObject

                                    val newAccessToken = jsonResponse["access_token"]?.jsonPrimitive?.content
                                    val newRefreshToken = jsonResponse["refresh_token"]?.jsonPrimitive?.content
                                    val userId = jsonResponse["user_id"]?.jsonPrimitive?.content?.toLongOrNull()

                                    if (newAccessToken != null && newRefreshToken != null && userId != null) {
                                        // Save new tokens
                                        tokenDataStore.saveTokens(
                                            accessToken = newAccessToken,
                                            refreshToken = newRefreshToken,
                                            userId = userId
                                        )

                                        // Retry original request with new token
                                        val newRequest = request.newBuilder()
                                            .header("Authorization", "Bearer $newAccessToken")
                                            .build()

                                        return@withLock chain.proceed(newRequest)
                                    }
                                }
                            }

                            refreshResponse.close()
                            // Refresh failed, clear tokens
                            tokenDataStore.clearTokens()
                        } catch (e: Exception) {
                            // Refresh failed, clear tokens
                            tokenDataStore.clearTokens()
                        }
                    }
                    null
                }
            }

            // Return new response if refresh was successful, otherwise return original 401
            if (newResponse != null) {
                return newResponse
            }
        }

        return response
    }
}