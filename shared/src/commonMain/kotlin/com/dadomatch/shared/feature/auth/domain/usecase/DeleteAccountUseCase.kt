package com.dadomatch.shared.feature.auth.domain.usecase

import com.dadomatch.shared.feature.auth.domain.repository.AuthRepository
import com.dadomatch.shared.feature.auth.presentation.NativeAuthHandler
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository

/**
 * Permanently deletes the signed-in user's account (App Store guideline
 * 5.1.1(v)). Firebase requires a recent login to delete, so this re-runs the
 * native sign-in for the user's provider to obtain a fresh credential, then
 * reauthenticates and deletes. A user-cancelled reauth prompt propagates as a
 * [com.dadomatch.shared.feature.auth.presentation.SignInCancelledException] and
 * must be treated as a dismissal, not an error.
 */
class DeleteAccountUseCase(
    private val authRepository: AuthRepository,
    private val nativeAuthHandler: NativeAuthHandler,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        val providerId = authRepository.getSignInProviderId()
            ?: return Result.failure(IllegalStateException("No signed-in provider to delete"))

        val result = when (providerId) {
            PROVIDER_GOOGLE -> {
                val tokens = nativeAuthHandler.signInWithGoogle().getOrElse { return Result.failure(it) }
                authRepository.deleteAccount(providerId, tokens.idToken, tokens.accessToken)
            }
            PROVIDER_APPLE -> {
                val tokens = nativeAuthHandler.signInWithApple().getOrElse { return Result.failure(it) }
                // Revoke the Apple token before deleting (guideline 5.1.1(v)) while
                // the user is still signed in. Best-effort: a revoke failure must
                // not trap the user — deletion is their primary intent.
                tokens.authorizationCode?.let { code ->
                    nativeAuthHandler.revokeAppleToken(code)
                }
                authRepository.deleteAccount(providerId, tokens.idToken, nonce = tokens.nonce)
            }
            else -> authRepository.deleteAccount(providerId, idToken = "")
        }

        // Reset RevenueCat to an anonymous user so the deleted account's
        // entitlements don't leak into the next session on this device.
        return result.onSuccess { subscriptionRepository.logOut() }
    }

    private companion object {
        const val PROVIDER_GOOGLE = "google.com"
        const val PROVIDER_APPLE = "apple.com"
    }
}
