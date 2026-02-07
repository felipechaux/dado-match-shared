package com.dadomatch.shared.feature.subscription.domain.model

/**
 * Feature entitlements that can be granted through subscriptions
 */
enum class Entitlement(val identifier: String) {
    /** Access to spicy dice categories */
    SPICY_DICE("spicy_dice"),
    
    /** Unlimited dice rolls (no daily limit) */
    UNLIMITED_ROLLS("unlimited_rolls"),
    
    /** Advanced success analytics and insights */
    SUCCESS_ANALYTICS("success_analytics"),
    /** Access to all premium features */
    PRO("chauxdevapps Pro"),
    
    /** Access to all premium categories */
    PREMIUM_CATEGORIES("premium_categories");
    
    companion object {
        /**
         * Get all entitlements for a subscription tier
         */
        fun forTier(tier: SubscriptionTier): Set<Entitlement> {
            return when (tier) {
                SubscriptionTier.FREE -> emptySet()
                SubscriptionTier.PREMIUM -> setOf(
                    PRO,
                    SPICY_DICE,
                    UNLIMITED_ROLLS,
                    SUCCESS_ANALYTICS,
                    PREMIUM_CATEGORIES
                )
            }
        }
        
        /**
         * Find entitlement by RevenueCat identifier
         */
        fun fromIdentifier(identifier: String): Entitlement? {
            return entries.find { it.identifier == identifier }
        }
    }
}
