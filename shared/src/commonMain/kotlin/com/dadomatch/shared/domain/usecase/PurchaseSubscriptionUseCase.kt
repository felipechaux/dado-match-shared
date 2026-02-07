package com.dadomatch.shared.domain.usecase

import com.dadomatch.shared.domain.model.SubscriptionStatus
import com.dadomatch.shared.domain.repository.SubscriptionRepository

/**
 * Use case to purchase a subscription
 */
class PurchaseSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository
) {
    /**
     * Purchase a subscription product
     * @param productId The product identifier to purchase
     * @return Result with updated subscription status
     */
    suspend operator fun invoke(productId: String): Result<SubscriptionStatus> {
        return subscriptionRepository.purchaseSubscription(productId)
    }
}
