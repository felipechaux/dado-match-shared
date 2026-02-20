package com.dadomatch.shared.feature.auth.presentation

interface NativeAuthHandler {
    suspend fun signInWithGoogle(): Result<GoogleTokens>
    suspend fun signInWithApple(): Result<AuthTokens>
}

/**
 * Tokens returned by a native Google Sign-In flow.
 * [accessToken] is required by Firebase iOS SDK via FIRGoogleAuthProvider.
 * On Android, it can be null (Firebase Android SDK accepts idToken-only).
 */
data class GoogleTokens(
    val idToken: String,
    val accessToken: String? = null
)

data class AuthTokens(
    val idToken: String,
    val nonce: String? = null
)
