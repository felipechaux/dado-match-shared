package com.dadomatch.shared.feature.subscription.domain.repository

import com.dadomatch.shared.feature.subscription.domain.model.Entitlement
import com.dadomatch.shared.feature.subscription.domain.model.Product
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing subscription and purchase operations
 */
interface SubscriptionRepository {
    /**
     * Get current subscription status as a Flow for reactive updates
     */
    fun getSubscriptionStatus(): Flow<SubscriptionStatus>
    
    /**
     * Get current subscription status (one-time)
     */
    suspend fun getCurrentSubscriptionStatus(): Result<SubscriptionStatus>
    
    /**
     * Purchase a subscription product
     * @param productId The product identifier to purchase
     * @return Result with updated subscription status
     */
    suspend fun purchaseSubscription(productId: String): Result<SubscriptionStatus>
    
    /**
     * Restore previous purchases
     * @return Result with updated subscription status
     */
    suspend fun restorePurchases(): Result<SubscriptionStatus>
    
    /**
     * Check if user has a specific entitlement
     * @param entitlement The entitlement to check
     * @return true if user has the entitlement
     */
    suspend fun hasEntitlement(entitlement: Entitlement): Boolean
    
    /**
     * Get available subscription products for purchase
     * @return Result with list of available products
     */
    suspend fun getAvailableProducts(): Result<List<Product>>
    
    /**
     * Decrement daily roll count for free users
     * @return Updated subscription status
     */
    suspend fun decrementDailyRolls(): Result<SubscriptionStatus>
    
    /**
     * Reset daily roll count (called at midnight)
     */
    suspend fun resetDailyRolls()

    /**
     * Link current user with RevenueCat ID
     */
    suspend fun logIn(userId: String): Result<SubscriptionStatus>

    /**
     * Unlink user from RevenueCat ID
     */
    suspend fun logOut(): Result<SubscriptionStatus>
}
