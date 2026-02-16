package com.dadomatch.shared.feature.auth.presentation

interface NativeAuthHandler {
    suspend fun signInWithGoogle(): Result<String>
    suspend fun signInWithApple(): Result<AuthTokens>
}

data class AuthTokens(
    val idToken: String,
    val nonce: String? = null
)
