package com.dadomatch.shared.feature.subscription.domain.model

/**
 * Represents the current subscription status of a user
 */
data class SubscriptionStatus(
    /** Current subscription tier */
    val tier: SubscriptionTier,
    
    /** Whether the subscription is active */
    val isActive: Boolean,
    
    /** Expiration date for the subscription (null for free tier or lifetime) */
    val expirationDate: kotlin.time.Instant? = null,
    
    /** Set of entitlements the user has access to */
    val entitlements: Set<Entitlement> = emptySet(),
    
    /** Whether the user is in a trial period */
    val isInTrialPeriod: Boolean = false,
    
    /** Number of dice rolls remaining today (null if unlimited) */
    val dailyRollsRemaining: Int? = null,
    
    /** Whether this is a lifetime subscription */
    val isLifetime: Boolean = false,
    
    /** Subscription renewal period identifier (e.g., "P1M") */
    val renewalPeriod: String? = null
) {
    /**
     * Check if user has a specific entitlement
     */
    fun hasEntitlement(entitlement: Entitlement): Boolean {
        return entitlements.contains(entitlement)
    }
    
    /**
     * Check if user can roll dice
     */
    fun canRollDice(): Boolean {
        // Premium users have unlimited rolls
        if (tier == SubscriptionTier.PREMIUM) return true
        
        // Free users must have rolls remaining
        return (dailyRollsRemaining ?: 0) > 0
    }
    
    companion object {
        /**
         * Default free tier subscription status
         */
        fun free(dailyRollsRemaining: Int = 10): SubscriptionStatus {
            return SubscriptionStatus(
                tier = SubscriptionTier.FREE,
                isActive = true,
                entitlements = Entitlement.forTier(SubscriptionTier.FREE),
                dailyRollsRemaining = dailyRollsRemaining
            )
        }

        /**
         * Default premium tier subscription status
         */
        fun premium(dailyRollsRemaining: Int? = null): SubscriptionStatus {
            return SubscriptionStatus(
                tier = SubscriptionTier.PREMIUM,
                isActive = true,
                entitlements = Entitlement.forTier(SubscriptionTier.PREMIUM),
                dailyRollsRemaining = dailyRollsRemaining
            )
        }
    }
}
