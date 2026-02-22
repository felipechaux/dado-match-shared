package com.dadomatch.shared.feature.subscription.data.remote

import com.dadomatch.shared.feature.subscription.domain.model.Entitlement
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.StoreProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Service wrapper around RevenueCat KMP SDK
 * Provides a clean interface for subscription operations.
 *
 * Note: Manual StateFlow is used to ensure UI reactivity across the app
 * when subscription status changes.
 */
class RevenueCatService {

    private val _customerInfoFlow = MutableStateFlow<CustomerInfo?>(null)
    val customerInfoFlow: Flow<CustomerInfo?> = _customerInfoFlow.asStateFlow()

    /**
     * Configure RevenueCat SDK
     * Should be called once at app startup
     */
    fun configure(apiKey: String, userId: String? = null) {
        val config = PurchasesConfiguration(apiKey) {
            if (userId != null) {
                appUserId = userId
            }
        }
        Purchases.configure(config)
    }

    /**
     * Get current customer info
     */
    suspend fun getCustomerInfo(): Result<CustomerInfo> = suspendCancellableCoroutine { continuation ->
        try {
            Purchases.sharedInstance.getCustomerInfo(
                onError = { error ->
                    continuation.resume(Result.failure(Exception(error.message)))
                },
                onSuccess = { customerInfo ->
                    _customerInfoFlow.value = customerInfo
                    continuation.resume(Result.success(customerInfo))
                }
            )
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Get available offerings (subscription products)
     */
    suspend fun getOfferings(): Result<Offerings> = suspendCancellableCoroutine { continuation ->
        try {
            Purchases.sharedInstance.getOfferings(
                onError = { error ->
                    continuation.resume(Result.failure(Exception(error.message)))
                },
                onSuccess = { offerings ->
                    continuation.resume(Result.success(offerings))
                }
            )
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Purchase a subscription product
     */
    suspend fun purchase(storeProduct: StoreProduct): Result<CustomerInfo> = suspendCancellableCoroutine { continuation ->
        try {
            Purchases.sharedInstance.purchase(
                storeProduct = storeProduct,
                onError = { error, _ ->
                    continuation.resume(Result.failure(Exception(error.message)))
                },
                onSuccess = { _, customerInfo ->
                    _customerInfoFlow.value = customerInfo
                    continuation.resume(Result.success(customerInfo))
                }
            )
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Restore previous purchases
     */
    suspend fun restorePurchases(): Result<CustomerInfo> = suspendCancellableCoroutine { continuation ->
        try {
            Purchases.sharedInstance.restorePurchases(
                onError = { error ->
                    continuation.resume(Result.failure(Exception(error.message)))
                },
                onSuccess = { customerInfo ->
                    _customerInfoFlow.value = customerInfo
                    continuation.resume(Result.success(customerInfo))
                }
            )
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Check if user has a specific entitlement
     */
    fun hasEntitlement(customerInfo: CustomerInfo?, entitlementId: String): Boolean {
        return customerInfo?.entitlements?.active?.containsKey(entitlementId) == true
    }

    /**
     * Link current user with an identifier (e.g. Firebase UID).
     * If RevenueCat creates a brand-new user for this ID (created=true),
     * it automatically restores purchases so any subscription made while anonymous
     * is properly carried over to the identified user.
     */
    suspend fun logIn(userId: String): Result<CustomerInfo> = suspendCancellableCoroutine { continuation ->
        try {
            Purchases.sharedInstance.logIn(
                onError = { error ->
                    continuation.resume(Result.failure(Exception(error.message)))
                },
                onSuccess = { customerInfo, created ->
                    if (created) {
                        // New RC user was created — restore purchases to link any existing
                        // store subscription (e.g. purchased while anonymous) to this ID.
                        Purchases.sharedInstance.restorePurchases(
                            onError = { _ ->
                                // Restore failed — fall back to the bare customerInfo
                                _customerInfoFlow.value = customerInfo
                                continuation.resume(Result.success(customerInfo))
                            },
                            onSuccess = { restoredInfo ->
                                _customerInfoFlow.value = restoredInfo
                                continuation.resume(Result.success(restoredInfo))
                            }
                        )
                    } else {
                        _customerInfoFlow.value = customerInfo
                        continuation.resume(Result.success(customerInfo))
                    }
                },
                newAppUserID = userId
            )
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Unlink user (log out)
     */
    suspend fun logOut(): Result<CustomerInfo> = suspendCancellableCoroutine { continuation ->
        try {
            Purchases.sharedInstance.logOut(
                onError = { error ->
                    continuation.resume(Result.failure(Exception(error.message)))
                },
                onSuccess = { customerInfo ->
                    _customerInfoFlow.value = customerInfo
                    continuation.resume(Result.success(customerInfo))
                }
            )
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Check if user has premium access
     */
    fun hasPremiumAccess(customerInfo: CustomerInfo?): Boolean {
        return hasEntitlement(customerInfo, Entitlement.PRO.identifier)
    }
}
