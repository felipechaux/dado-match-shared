package com.dadomatch.shared.feature.auth.presentation

import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of [NativeAuthHandler].
 *
 * Sign-in flows are driven natively (Swift-side) via providers set on this handler.
 * When the KMP layer calls signInWithGoogle/Apple, the coroutine suspends until Swift
 * resolves the native sign-in and calls onSuccess/onFailure on the callback.
 */
class IosAuthHandler : NativeAuthHandler {

    /** Set by Swift (AuthCoordinator) before any Google Sign-In is triggered. */
    var googleSignInProvider: ((GoogleSignInCallback) -> Unit)? = null

    /** Set by Swift (AuthCoordinator) before any Apple Sign-In is triggered. */
    var appleSignInProvider: ((AppleSignInCallback) -> Unit)? = null

    override suspend fun signInWithGoogle(): Result<GoogleTokens> {
        val provider = googleSignInProvider
            ?: return Result.failure(Exception("Google Sign-In provider not set. Ensure AuthCoordinator.setup() is called after initKoin()."))

        return suspendCoroutine { continuation ->
            provider(object : GoogleSignInCallback {
                override fun onSuccess(idToken: String, accessToken: String) {
                    continuation.resumeWith(
                        kotlin.Result.success(Result.success(GoogleTokens(idToken, accessToken)))
                    )
                }

                override fun onFailure(error: String) {
                    continuation.resumeWith(
                        kotlin.Result.success(Result.failure(Exception(error)))
                    )
                }
            })
        }
    }

    override suspend fun signInWithApple(): Result<AuthTokens> {
        val provider = appleSignInProvider
            ?: return Result.failure(Exception("Apple Sign-In provider not set. Ensure AuthCoordinator.setup() is called after initKoin()."))

        return suspendCoroutine { continuation ->
            provider(object : AppleSignInCallback {
                override fun onSuccess(idToken: String, nonce: String?) {
                    continuation.resumeWith(
                        kotlin.Result.success(Result.success(AuthTokens(idToken, nonce)))
                    )
                }

                override fun onFailure(error: String) {
                    continuation.resumeWith(
                        kotlin.Result.success(Result.failure(Exception(error)))
                    )
                }
            })
        }
    }
}
