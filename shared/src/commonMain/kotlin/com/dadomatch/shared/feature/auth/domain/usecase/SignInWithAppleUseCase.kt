package com.dadomatch.shared.feature.auth.domain.usecase

import com.dadomatch.shared.feature.auth.domain.repository.AuthRepository
import com.dadomatch.shared.feature.auth.domain.repository.AuthUser
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository

class SignInWithAppleUseCase(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(idToken: String, nonce: String? = null): Result<AuthUser> =
        authRepository.signInWithApple(idToken, nonce)
            .onSuccess { user -> subscriptionRepository.logIn(user.id) }
}
