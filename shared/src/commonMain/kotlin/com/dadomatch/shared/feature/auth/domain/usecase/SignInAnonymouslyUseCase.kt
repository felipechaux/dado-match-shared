package com.dadomatch.shared.feature.auth.domain.usecase

import com.dadomatch.shared.feature.auth.domain.repository.AuthRepository
import com.dadomatch.shared.feature.auth.domain.repository.AuthUser

class SignInAnonymouslyUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<AuthUser> =
        authRepository.signInAnonymously()
}
