package com.dadomatch.shared.feature.subscription.data.mapper

import com.dadomatch.shared.feature.subscription.domain.model.Entitlement
import com.dadomatch.shared.feature.subscription.domain.model.Product
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionStatus
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PeriodType
import com.revenuecat.purchases.kmp.models.StoreProduct
import kotlin.time.ExperimentalTime

/**
 * Map RevenueCat CustomerInfo to domain SubscriptionStatus
 */
@OptIn(ExperimentalTime::class)
fun CustomerInfo.toSubscriptionStatus(dailyRollsRemaining: Int?): SubscriptionStatus {
    val proEntitlementId = Entitlement.PRO.identifier
    val hasPremium = entitlements.active.containsKey(proEntitlementId)
    
    val baseStatus = if (hasPremium) {
        SubscriptionStatus.premium(dailyRollsRemaining)
    } else {
        SubscriptionStatus.free(dailyRollsRemaining ?: 10)
    }
    
    val entitlementSet = entitlements.active.keys.mapNotNull { key ->
        Entitlement.fromIdentifier(key)
    }.toSet() + baseStatus.entitlements
    
    val entitlementInfo = entitlements.active[proEntitlementId]
    val expirationDate = entitlementInfo?.expirationDate
    val renewalPeriod = entitlementInfo?.productPlanIdentifier // This is often used for plan IDs but RC KMP might differ. 
    // Usually one might need to match with products or use another field. 
    // In RC KMP EntitlementInfo, periodic info isn't as direct as StoreProduct.
    
    // Check if in trial period
    val isInTrial = entitlementInfo?.periodType == PeriodType.TRIAL
    
    return baseStatus.copy(
        isActive = hasPremium,
        expirationDate = expirationDate,
        entitlements = entitlementSet,
        isInTrialPeriod = isInTrial,
        isLifetime = hasPremium && expirationDate == null,
        renewalPeriod = renewalPeriod 
    )
}

/**
 * Map RevenueCat StoreProduct to domain Product
 */
fun StoreProduct.toProduct(): Product {
    // Get subscription period from the product's period
    val periodString = period?.unit?.name?.let { unit ->
        val value = period?.value ?: 1
        when (unit.uppercase()) {
            "MONTH" -> if (value == 1) "P1M" else "P${value}M"
            "YEAR" -> if (value == 1) "P1Y" else "P${value}Y"
            "WEEK" -> if (value == 1) "P1W" else "P${value}W"
            "DAY" -> if (value == 1) "P1D" else "P${value}D"
            else -> null
        }
    }
    
    return Product(
        id = id,
        title = title,
        description = presentedOfferingContext?.offeringIdentifier ?: title,
        priceString = price.formatted,
        priceAmountMicros = price.amountMicros,
        priceCurrencyCode = price.currencyCode,
        subscriptionPeriod = periodString,
        freeTrialPeriod = null, // Trial info not easily accessible in this version
        hasFreeTrialAvailable = false
    )
}
