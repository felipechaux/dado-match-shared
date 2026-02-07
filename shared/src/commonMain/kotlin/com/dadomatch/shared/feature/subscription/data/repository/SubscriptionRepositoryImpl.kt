package com.dadomatch.shared.feature.subscription.data.repository

import com.dadomatch.shared.feature.subscription.data.local.SubscriptionLocalDataSource
import com.dadomatch.shared.feature.subscription.data.mapper.toProduct
import com.dadomatch.shared.feature.subscription.data.mapper.toSubscriptionStatus
import com.dadomatch.shared.feature.subscription.data.remote.RevenueCatService
import com.dadomatch.shared.feature.subscription.domain.model.Entitlement
import com.dadomatch.shared.feature.subscription.domain.model.Product
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionStatus
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * Implementation of SubscriptionRepository
 */
class SubscriptionRepositoryImpl(
    private val revenueCatService: RevenueCatService,
    private val localDataSource: SubscriptionLocalDataSource
) : SubscriptionRepository {
    
    override fun getSubscriptionStatus(): Flow<SubscriptionStatus> {
        return combine(
            revenueCatService.customerInfoFlow,
            localDataSource.getDailyRollsRemaining()
        ) { customerInfo, dailyRolls ->
            customerInfo?.toSubscriptionStatus(dailyRolls) 
                ?: SubscriptionStatus.free(dailyRolls)
        }
    }
    
    override suspend fun getCurrentSubscriptionStatus(): Result<SubscriptionStatus> {
        return try {
            val customerInfoResult = revenueCatService.getCustomerInfo()
            val dailyRolls = localDataSource.getDailyRollsRemaining().first()
            
            customerInfoResult.map { customerInfo ->
                customerInfo.toSubscriptionStatus(dailyRolls)
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
            // Check if user is premium first
            val customerInfoResult = revenueCatService.getCustomerInfo()
            if (customerInfoResult.isFailure) {
                return Result.failure(customerInfoResult.exceptionOrNull()!!)
            }
            
            val customerInfo = customerInfoResult.getOrNull()
            val hasPremium = revenueCatService.hasPremiumAccess(customerInfo)
            
            // Premium users don't have roll limits
            if (hasPremium) {
                val dailyRolls = localDataSource.getDailyRollsRemaining().first()
                return Result.success(customerInfo!!.toSubscriptionStatus(null))
            }
            
            // Decrement for free users
            val newCount = localDataSource.decrementDailyRolls()
            Result.success(customerInfo!!.toSubscriptionStatus(newCount))
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
}
