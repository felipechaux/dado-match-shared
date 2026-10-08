package com.dadomatch.shared.feature.icebreaker.domain.usecase

import com.dadomatch.shared.core.util.Resource
import com.dadomatch.shared.feature.icebreaker.data.telemetry.AiTelemetry
import com.dadomatch.shared.feature.icebreaker.data.telemetry.NoOpAiTelemetry
import com.dadomatch.shared.feature.icebreaker.domain.repository.IcebreakerRepository
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository
import kotlinx.coroutines.CancellationException

class GenerateIcebreakerUseCase(
    private val repository: IcebreakerRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val telemetry: AiTelemetry = NoOpAiTelemetry,
) {
    suspend operator fun invoke(environment: String, intensity: String, language: String): Resource<String> {
        // Wrap everything reachable from this use case so subscription-repo failures
        // (DB read, decrement transaction, etc.) also land in Crashlytics — the router's
        // safety net only covers the AI call itself.
        return try {
            run(environment, intensity, language)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            telemetry.onUnexpectedError(stage = "generate_icebreaker_use_case", cause = e)
            Resource.Error(e.message ?: "ai_unexpected_error", e)
        }
    }

    private suspend fun run(environment: String, intensity: String, language: String): Resource<String> {
        // Refilled lazily, like the free rolls: nothing runs at midnight.
        subscriptionRepository.resetDailyAiCalls()
        val status = subscriptionRepository.getCurrentSubscriptionStatus().getOrNull()
            ?: return Resource.Error("no_ai_calls_available")

        // Free users are limited by their daily rolls (already spent by RollDiceUseCase);
        // the AI call budget below only applies to premium.
        if (status.tier != SubscriptionTier.FREE) {
            if (status.dailyAiCallsRemaining <= 0) {
                return Resource.Error("daily_ai_limit_reached")
            }
            // Decrement the counter before making the call
            subscriptionRepository.decrementDailyAiCalls()
        }

        return repository.generateIcebreaker(
            environment = environment,
            intensity = intensity,
            language = language,
            usePremiumModel = status.isLifetime
        )
    }
}
