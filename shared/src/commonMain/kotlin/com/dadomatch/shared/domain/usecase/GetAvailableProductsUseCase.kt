package com.dadomatch.shared.domain.usecase

import com.dadomatch.shared.domain.model.Product
import com.dadomatch.shared.domain.repository.SubscriptionRepository

/**
 * Use case to get available subscription products
 */
class GetAvailableProductsUseCase(
    private val subscriptionRepository: SubscriptionRepository
) {
    /**
     * Get available subscription products for purchase
     * @return Result with list of available products
     */
    suspend operator fun invoke(): Result<List<Product>> {
        return subscriptionRepository.getAvailableProducts()
    }
}
