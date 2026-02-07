package com.dadomatch.shared.domain.usecase

import com.dadomatch.shared.domain.model.SubscriptionStatus
import com.dadomatch.shared.domain.repository.SubscriptionRepository

/**
 * Use case to restore previous purchases
 */
class RestorePurchasesUseCase(
    private val subscriptionRepository: SubscriptionRepository
) {
    /**
     * Restore previous purchases
     * @return Result with updated subscription status
     */
    suspend operator fun invoke(): Result<SubscriptionStatus> {
        return subscriptionRepository.restorePurchases()
    }
}
