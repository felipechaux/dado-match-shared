package com.dadomatch.shared.feature.subscription.domain.usecase

import com.dadomatch.shared.feature.subscription.domain.model.Entitlement
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository

/**
 * Use case to check if user has a specific entitlement
 */
class CheckEntitlementUseCase(
    val subscriptionRepository: SubscriptionRepository
) {
    /**
     * Check if user has access to a specific feature
     * @param entitlement The entitlement to check
     * @return true if user has the entitlement
     */
    suspend operator fun invoke(entitlement: Entitlement): Boolean {
        return subscriptionRepository.hasEntitlement(entitlement)
    }
    
    /**
     * Check if user can access spicy dice categories
     */
    suspend fun canAccessSpicyDice(): Boolean {
        return invoke(Entitlement.SPICY_DICE)
    }
    
    /**
     * Check if user has unlimited rolls
     */
    suspend fun hasUnlimitedRolls(): Boolean {
        return invoke(Entitlement.UNLIMITED_ROLLS)
    }
    
    /**
     * Check if user can access success analytics
     */
    suspend fun canAccessAnalytics(): Boolean {
        return invoke(Entitlement.SUCCESS_ANALYTICS)
    }
    
    /**
     * Check if user can access premium categories
     */
    suspend fun canAccessPremiumCategories(): Boolean {
        return invoke(Entitlement.PREMIUM_CATEGORIES)
    }

    /**
     * Check if a specific category is considered "spicy"
     */
    fun isSpicyCategory(category: String): Boolean {
        return category == "int_spicy"
    }

    /**
     * Check if user can access a specific category
     */
    suspend fun canAccessCategory(category: String): Boolean {
        return if (isSpicyCategory(category)) {
            canAccessSpicyDice()
        } else {
            true
        }
    }
}
