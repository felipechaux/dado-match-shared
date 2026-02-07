package com.dadomatch.shared.di

import com.dadomatch.shared.core.di.coreModule
import com.dadomatch.shared.feature.icebreaker.di.icebreakerModule
import com.dadomatch.shared.feature.onboarding.di.onboardingModule
import com.dadomatch.shared.feature.subscription.di.subscriptionModule
import com.dadomatch.shared.feature.success.di.successModule

/**
 * Returns a list of all Koin modules in the application,
 * structured by feature responsibility.
 */
fun getAllModules() = listOf(
    coreModule,
    icebreakerModule,
    successModule,
    subscriptionModule,
    onboardingModule
)
