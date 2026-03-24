package com.dadomatch.shared.feature.icebreaker.domain.usecase

import com.dadomatch.shared.core.util.Resource
import com.dadomatch.shared.feature.icebreaker.domain.repository.IcebreakerRepository
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository

class GenerateIcebreakerUseCase(
    private val repository: IcebreakerRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(environment: String, intensity: String, language: String): Resource<String> {
        val status = subscriptionRepository.getCurrentSubscriptionStatus().getOrNull()

        // Free (or unauthenticated) users cannot generate icebreakers
        if (status == null || status.tier == SubscriptionTier.FREE) {
            return Resource.Error("no_ai_calls_available")
        }

        // Block if the daily AI call budget is exhausted
        if (status.dailyAiCallsRemaining <= 0) {
            return Resource.Error("daily_ai_limit_reached")
        }

        // Decrement the counter before making the call
        subscriptionRepository.decrementDailyAiCalls()

        return repository.generateIcebreaker(
            environment = environment,
            intensity = intensity,
            language = language,
            usePremiumModel = status.isLifetime
        )
    }
}
