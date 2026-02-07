package com.dadomatch.shared.feature.subscription.domain.model

/**
 * Represents a subscription product available for purchase
 */
data class Product(
    /** Unique product identifier */
    val id: String,
    
    /** Display title */
    val title: String,
    
    /** Product description */
    val description: String,
    
    /** Formatted price string (e.g., "$4.99") */
    val priceString: String,
    
    /** Price in cents/smallest currency unit */
    val priceAmountMicros: Long,
    
    /** Currency code (e.g., "USD") */
    val priceCurrencyCode: String,
    
    /** Subscription period (e.g., "P1M" for monthly, "P1Y" for yearly) */
    val subscriptionPeriod: String? = null,
    
    /** Free trial period (e.g., "P7D" for 7 days) */
    val freeTrialPeriod: String? = null,
    
    /** Whether this product offers a free trial */
    val hasFreeTrialAvailable: Boolean = false
)
