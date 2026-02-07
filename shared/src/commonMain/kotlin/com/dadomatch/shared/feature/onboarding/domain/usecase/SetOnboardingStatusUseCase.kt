package com.dadomatch.shared.feature.onboarding.domain.usecase

import com.dadomatch.shared.feature.onboarding.domain.repository.PreferenceRepository

class SetOnboardingStatusUseCase(private val preferenceRepository: PreferenceRepository) {
    suspend operator fun invoke(completed: Boolean) = preferenceRepository.setOnboardingCompleted(completed)
}
