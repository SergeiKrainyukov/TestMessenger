package com.skrainyukov.testmessenger.data.remote.interceptor

import com.skrainyukov.testmessenger.data.local.datastore.TokenDataStore
import com.skrainyukov.testmessenger.data.remote.HttpHeaders
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

        // Check if request has "No-Authentication" header
        val noAuth = originalRequest.header(HttpHeaders.NO_AUTHENTICATION)
        if (noAuth != null) {
            // Remove the marker header and proceed without auth
            val requestWithoutMarker = originalRequest.newBuilder()
                .removeHeader(HttpHeaders.NO_AUTHENTICATION)
                .build()
            return chain.proceed(requestWithoutMarker)
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