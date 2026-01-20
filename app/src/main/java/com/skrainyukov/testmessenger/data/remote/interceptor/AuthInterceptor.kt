package com.skrainyukov.testmessenger.data.remote.interceptor

import com.skrainyukov.testmessenger.data.local.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // Skip auth for these endpoints
        if (url.contains("/send-auth-code/") ||
            url.contains("/check-auth-code/") ||
            url.contains("/register/") ||
            url.contains("/refresh-token/")
        ) {
            return chain.proceed(originalRequest)
        }

        // Add access token to request
        val accessToken = runBlocking {
            tokenDataStore.accessToken.first()
        }

        val requestWithAuth = if (accessToken != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(requestWithAuth)
    }
}