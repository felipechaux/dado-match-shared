package com.dadomatch.shared.domain.usecase

import com.dadomatch.shared.domain.model.SubscriptionStatus
import com.dadomatch.shared.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get current subscription status
 */
class GetSubscriptionStatusUseCase(
    private val subscriptionRepository: SubscriptionRepository
) {
    /**
     * Get subscription status as a Flow for reactive updates
     */
    operator fun invoke(): Flow<SubscriptionStatus> {
        return subscriptionRepository.getSubscriptionStatus()
    }
    
    /**
     * Get current subscription status (one-time)
     */
    suspend fun getCurrentStatus(): Result<SubscriptionStatus> {
        return subscriptionRepository.getCurrentSubscriptionStatus()
    }
}
