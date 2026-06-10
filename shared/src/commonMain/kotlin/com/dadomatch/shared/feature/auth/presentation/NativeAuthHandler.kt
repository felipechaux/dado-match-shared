package com.dadomatch.shared.feature.auth.presentation

interface NativeAuthHandler {
    suspend fun signInWithGoogle(): Result<GoogleTokens>
    suspend fun signInWithApple(): Result<AuthTokens>
}

/**
 * The user dismissed the native sign-in UI (e.g. cancelled the system consent
 * dialog or the account picker). Not a real failure — must never be surfaced
 * to the user as an error message.
 */
class SignInCancelledException : Exception("Sign in cancelled")

/**
 * Error code the native (Swift) layer sends through the sign-in callbacks when
 * the user cancels. Part of the Swift <-> Kotlin contract; keep in sync with
 * AuthCoordinator.swift in the iOS app.
 */
const val NATIVE_SIGN_IN_CANCELLED = "CANCELLED"

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
