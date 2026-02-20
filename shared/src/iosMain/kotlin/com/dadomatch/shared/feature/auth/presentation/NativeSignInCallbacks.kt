package com.dadomatch.shared.feature.auth.presentation

/**
 * Callback interface exposed to Swift so the native iOS layer can deliver
 * Google Sign-In results back into Kotlin suspend functions.
 * [accessToken] is required on iOS by FIRGoogleAuthProvider.credential(withIDToken:accessToken:).
 */
interface GoogleSignInCallback {
    fun onSuccess(idToken: String, accessToken: String)
    fun onFailure(error: String)
}

/**
 * Callback interface exposed to Swift for Apple Sign-In results.
 */
interface AppleSignInCallback {
    fun onSuccess(idToken: String, nonce: String?)
    fun onFailure(error: String)
}
