package com.dadomatch.shared.feature.onboarding.di

import com.dadomatch.shared.feature.onboarding.data.repository.PreferenceRepositoryImpl
import com.dadomatch.shared.feature.onboarding.domain.repository.PreferenceRepository
import com.dadomatch.shared.feature.onboarding.domain.usecase.GetOnboardingStatusUseCase
import com.dadomatch.shared.feature.onboarding.domain.usecase.SetOnboardingStatusUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Feature: Onboarding
 */
val onboardingModule = module {
    singleOf(::PreferenceRepositoryImpl) bind PreferenceRepository::class
    factoryOf(::GetOnboardingStatusUseCase)
    factoryOf(::SetOnboardingStatusUseCase)
}
