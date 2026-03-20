package com.dadomatch.shared.feature.auth.domain.usecase

import com.dadomatch.shared.feature.auth.domain.repository.AuthRepository
import com.dadomatch.shared.feature.auth.domain.repository.AuthUser
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(idToken: String, accessToken: String? = null): Result<AuthUser> =
        authRepository.signInWithGoogle(idToken, accessToken)
            .onSuccess { user -> subscriptionRepository.logIn(user.id) }
}
