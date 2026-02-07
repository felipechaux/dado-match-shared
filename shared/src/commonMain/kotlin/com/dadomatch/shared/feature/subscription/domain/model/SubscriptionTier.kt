package com.dadomatch.shared.feature.subscription.domain.model

/**
 * Subscription tier levels available in the app
 */
enum class SubscriptionTier {
    /** Free tier with limited features */
    FREE,
    
    /** Premium tier with all features unlocked */
    PREMIUM;
    
    val displayName: String
        get() = when (this) {
            FREE -> "Free"
            PREMIUM -> "Premium"
        }
}
