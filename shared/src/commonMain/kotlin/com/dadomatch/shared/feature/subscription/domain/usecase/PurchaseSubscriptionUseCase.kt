package com.dadomatch.shared.feature.subscription.domain.usecase

import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionStatus
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository

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
