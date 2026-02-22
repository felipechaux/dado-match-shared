package com.dadomatch.shared.feature.subscription.data.repository

import com.dadomatch.shared.feature.subscription.data.local.SubscriptionLocalDataSource
import com.dadomatch.shared.feature.subscription.data.mapper.toProduct
import com.dadomatch.shared.feature.subscription.data.mapper.toSubscriptionStatus
import com.dadomatch.shared.feature.subscription.data.remote.RevenueCatService
import com.dadomatch.shared.feature.subscription.domain.model.Entitlement
import com.dadomatch.shared.feature.subscription.domain.model.Product
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionStatus
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository
import com.dadomatch.shared.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transformLatest

/**
 * Implementation of SubscriptionRepository
 */
class SubscriptionRepositoryImpl(
    private val revenueCatService: RevenueCatService,
    private val localDataSource: SubscriptionLocalDataSource,
    private val authRepository: AuthRepository
) : SubscriptionRepository {
    
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getSubscriptionStatus(): Flow<SubscriptionStatus> {
        // transformLatest reacts to every auth state change.
        // null user = Firebase not yet loaded or unauthenticated — treat as guest.
        return authRepository.currentUser.transformLatest { user ->
            // null or anonymous → guest: drive status purely from local DataStore
            val isLoggedIn = user != null && !user.isAnonymous

            if (!isLoggedIn) {
                // Guest/anonymous: roll counter lives entirely in local DataStore
                localDataSource.getDailyRollsRemaining().collect { dailyRolls ->
                    emit(SubscriptionStatus.free(dailyRolls))
                }
            } else {
                // Logged-in identified user: combine cached RC info + live local roll count.
                // No network calls inside the flow — counter must update instantly on decrement.
                combine(
                    revenueCatService.customerInfoFlow,
                    localDataSource.getDailyRollsRemaining()
                ) { customerInfo, dailyRolls ->
                    if (customerInfo != null) {
                        // Use cached RC data to determine Pro vs Free
                        customerInfo.toSubscriptionStatus(dailyRolls)
                    } else {
                        // RC not fetched yet — emit Free immediately.
                        // loadSubscriptionData() will call getCustomerInfo() on startup,
                        // which updates customerInfoFlow and triggers an automatic re-emit.
                        SubscriptionStatus.free(dailyRolls)
                    }
                }.collect { status -> emit(status) }
            }
        }
    }

    override suspend fun getCurrentSubscriptionStatus(): Result<SubscriptionStatus> {
        return try {
            // Get current Firebase auth state — may be null if still loading
            val user = authRepository.currentUser.first()
            val isAnonymous = user?.isAnonymous ?: true
            val dailyRolls = localDataSource.getDailyRollsRemaining().first()

            if (isAnonymous) {
                Result.success(SubscriptionStatus.free(dailyRolls))
            } else {
                // Fetch fresh data from RevenueCat for this real user
                val customerInfoResult = revenueCatService.getCustomerInfo()
                customerInfoResult.map { customerInfo ->
                    customerInfo.toSubscriptionStatus(dailyRolls)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun purchaseSubscription(productId: String): Result<SubscriptionStatus> {
        return try {
            // First get the product
            val offeringsResult = revenueCatService.getOfferings()
            if (offeringsResult.isFailure) {
                return Result.failure(offeringsResult.exceptionOrNull()!!)
            }
            
            val offerings = offeringsResult.getOrNull()
            val storeProduct = offerings?.current?.availablePackages
                ?.map { it.storeProduct }
                ?.find { it.id == productId }
            
            if (storeProduct == null) {
                return Result.failure(Exception("Product not found: $productId"))
            }
            
            // Purchase the product
            val purchaseResult = revenueCatService.purchase(storeProduct)
            val dailyRolls = localDataSource.getDailyRollsRemaining().first()
            
            purchaseResult.map { customerInfo ->
                val status = customerInfo.toSubscriptionStatus(dailyRolls)
                // Cache premium status
                localDataSource.setPremiumStatus(status.tier == com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier.PREMIUM)
                status
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun restorePurchases(): Result<SubscriptionStatus> {
        return try {
            val restoreResult = revenueCatService.restorePurchases()
            val dailyRolls = localDataSource.getDailyRollsRemaining().first()
            
            restoreResult.map { customerInfo ->
                val status = customerInfo.toSubscriptionStatus(dailyRolls)
                // Cache premium status
                localDataSource.setPremiumStatus(status.tier == com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier.PREMIUM)
                status
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun hasEntitlement(entitlement: Entitlement): Boolean {
        return try {
            val status = getCurrentSubscriptionStatus().getOrNull()
            status?.hasEntitlement(entitlement) ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getAvailableProducts(): Result<List<Product>> {
        return try {
            val offeringsResult = revenueCatService.getOfferings()
            
            offeringsResult.map { offerings ->
                offerings.current?.availablePackages?.map { pkg ->
                    pkg.storeProduct.toProduct()
                } ?: emptyList()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun decrementDailyRolls(): Result<SubscriptionStatus> {
        return try {
            val user = authRepository.currentUser.first()
            val isAnonymous = user?.isAnonymous ?: true

            // Use cached customerInfo to avoid a blocking network call on every roll
            val cachedCustomerInfo = revenueCatService.customerInfoFlow.first()
            val hasPremium = if (isAnonymous) {
                false
            } else {
                revenueCatService.hasPremiumAccess(cachedCustomerInfo)
            }

            val dailyRolls = localDataSource.getDailyRollsRemaining().first()

            // Premium users have unlimited rolls — don't decrement
            if (hasPremium) {
                return Result.success(
                    cachedCustomerInfo?.toSubscriptionStatus(dailyRolls)
                        ?: SubscriptionStatus.free(dailyRolls)
                )
            }

            // Decrement for free users
            val newCount = localDataSource.decrementDailyRolls()
            Result.success(
                cachedCustomerInfo?.toSubscriptionStatus(newCount)
                    ?: SubscriptionStatus.free(newCount)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun resetDailyRolls() {
        // Check if reset is needed
        if (localDataSource.shouldResetDailyRolls()) {
            localDataSource.resetDailyRolls()
        }
    }

    override suspend fun logIn(userId: String): Result<SubscriptionStatus> {
        return try {
            val loginResult = revenueCatService.logIn(userId)
            val dailyRolls = localDataSource.getDailyRollsRemaining().first()
            
            loginResult.map { customerInfo ->
                customerInfo.toSubscriptionStatus(dailyRolls)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logOut(): Result<SubscriptionStatus> {
        return try {
            val logoutResult = revenueCatService.logOut()
            val dailyRolls = localDataSource.getDailyRollsRemaining().first()
            
            logoutResult.map { customerInfo ->
                customerInfo.toSubscriptionStatus(dailyRolls)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
