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
 * [authorizationCode] is needed to revoke the Apple token on account deletion
 * (App Store guideline 5.1.1(v)); like the name, Apple provides it fresh on each
 * authorization.
 */
interface AppleSignInCallback {
    fun onSuccess(idToken: String, nonce: String?, displayName: String?, authorizationCode: String?)
    fun onFailure(error: String)
}

/**
 * Callback interface exposed to Swift for the result of revoking a Sign in with
 * Apple token via the Firebase iOS SDK (`Auth.auth().revokeToken`).
 */
interface AppleRevokeCallback {
    fun onSuccess()
    fun onFailure(error: String)
}
