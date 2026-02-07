package com.dadomatch.shared.feature.onboarding.domain.usecase

import com.dadomatch.shared.feature.onboarding.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetOnboardingStatusUseCase(private val preferenceRepository: PreferenceRepository) {
    operator fun invoke(): Flow<Boolean> = preferenceRepository.isOnboardingCompleted()
}
