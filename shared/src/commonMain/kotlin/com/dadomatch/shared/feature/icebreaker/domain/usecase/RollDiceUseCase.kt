package com.dadomatch.shared.feature.icebreaker.domain.usecase

import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionStatus
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository
import com.dadomatch.shared.feature.subscription.domain.usecase.CheckEntitlementUseCase

/**
 * Use case to handle dice roll with subscription checks
 */
class RollDiceUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val checkEntitlementUseCase: CheckEntitlementUseCase
) {
    /**
     * Attempt to roll dice, checking subscription limits
     * @return Result with updated subscription status if successful, or error if no rolls remaining
     */
    suspend operator fun invoke(): Result<SubscriptionStatus> {
        // Get current status
        val currentStatus = subscriptionRepository.getCurrentSubscriptionStatus()
        if (currentStatus.isFailure) {
            return currentStatus
        }
        
        val status = currentStatus.getOrNull()!!
        
        // Premium users with UNLIMITED_ROLLS skip limits
        if (checkEntitlementUseCase.hasUnlimitedRolls()) {
            return Result.success(status)
        }
        
        // Check if user can roll
        if (!status.canRollDice()) {
            return Result.failure(
                NoRollsRemainingException("You have no dice rolls remaining today. Upgrade to Premium for unlimited rolls!")
            )
        }
        
        // Decrement rolls for free users
        return subscriptionRepository.decrementDailyRolls()
    }
    
    /**
     * Reset daily rolls (should be called at midnight)
     */
    suspend fun resetDailyRolls() {
        subscriptionRepository.resetDailyRolls()
    }
}

/**
 * Exception thrown when user has no rolls remaining
 */
class NoRollsRemainingException(message: String) : Exception(message)
