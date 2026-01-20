package com.skrainyukov.testmessenger.data.remote.interceptor

import com.skrainyukov.testmessenger.data.local.datastore.TokenDataStore
import com.skrainyukov.testmessenger.data.remote.api.AuthApi
import com.skrainyukov.testmessenger.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val authApi: AuthApi
) : Interceptor {

    private val mutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // If 401 and not a refresh/auth endpoint, try to refresh token
        if (response.code == 401 && !request.url.toString().contains("/refresh-token/")) {
            response.close()

            runBlocking {
                mutex.withLock {
                    val refreshToken = tokenDataStore.refreshToken.first()

                    if (refreshToken != null) {
                        try {
                            val refreshResponse = authApi.refreshToken(
                                RefreshTokenRequest(refreshToken)
                            )

                            // Save new tokens
                            tokenDataStore.saveTokens(
                                accessToken = refreshResponse.accessToken,
                                refreshToken = refreshResponse.refreshToken,
                                userId = refreshResponse.userId
                            )

                            // Retry original request with new token
                            val newRequest = request.newBuilder()
                                .header("Authorization", "Bearer ${refreshResponse.accessToken}")
                                .build()

                            return@withLock chain.proceed(newRequest)
                        } catch (e: Exception) {
                            // Refresh failed, clear tokens
                            tokenDataStore.clearTokens()
                        }
                    }
                }
            }
        }

        return response
    }
}