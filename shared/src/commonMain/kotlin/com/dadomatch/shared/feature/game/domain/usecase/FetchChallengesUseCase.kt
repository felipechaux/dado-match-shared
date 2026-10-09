package com.dadomatch.shared.feature.game.domain.usecase

import com.dadomatch.shared.feature.game.domain.Challenge
import com.dadomatch.shared.feature.game.domain.DateLevel
import com.dadomatch.shared.feature.game.domain.GameConfig
import com.dadomatch.shared.feature.game.domain.repository.GameRepository
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository
import kotlinx.coroutines.CancellationException

/**
 * A batch of challenges: from the AI when possible, otherwise from the offline deck.
 * Never fails — a game must not stop because the network did.
 *
 * Each batch is one AI call, so it counts against Pro users' daily AI budget like an
 * icebreaker does; once that is spent they get the offline deck.
 */
class FetchChallengesUseCase(
    private val repository: GameRepository,
    private val subscriptionRepository: SubscriptionRepository,
) {
    suspend operator fun invoke(config: GameConfig, level: DateLevel?, avoid: List<String>): List<Challenge> {
        val generated = try {
            generate(config, level, avoid)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            null
        }
        if (generated != null) return generated

        val offline = repository.offlineChallenges(config, level).shuffled()
        // A long game can go through the whole deck: start it over rather than stop
        return offline.filter { it.text !in avoid }.ifEmpty { offline }
    }

    private suspend fun generate(config: GameConfig, level: DateLevel?, avoid: List<String>): List<Challenge>? {
        subscriptionRepository.resetDailyAiCalls()
        val status = subscriptionRepository.getCurrentSubscriptionStatus().getOrNull()
        if (status != null && status.tier != SubscriptionTier.FREE) {
            if (status.dailyAiCallsRemaining <= 0) return null
            subscriptionRepository.decrementDailyAiCalls()
        }
        return repository.generateChallenges(config, level, avoid, usePremiumModel = status?.isLifetime == true)
    }
}
