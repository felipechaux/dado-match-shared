package com.dadomatch.shared.feature.onboarding.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
}
