package com.dadomatch.shared.feature.game.domain.usecase

import com.dadomatch.shared.feature.game.domain.GameConfig
import com.dadomatch.shared.feature.game.domain.GameRules
import com.dadomatch.shared.feature.game.domain.repository.GameRepository
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

sealed interface StartGameResult {
    data class Started(val isPro: Boolean) : StartGameResult
    data object ToneLocked : StartGameResult
    data object DailyLimitReached : StartGameResult
}

/** Applies the free limits (Funny tone only, one game a day) and remembers the group. */
class StartGameUseCase(
    private val repository: GameRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(config: GameConfig): StartGameResult {
        val status = subscriptionRepository.getCurrentSubscriptionStatus().getOrNull()
        val isPro = status != null && status.tier != SubscriptionTier.FREE
        val today = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        if (!isPro) {
            if (config.tone.requiresPro) return StartGameResult.ToneLocked
            if (repository.gamesStartedOn(today) >= GameRules.FREE_GAMES_PER_DAY) return StartGameResult.DailyLimitReached
        }
        repository.recordGameStarted(today)
        repository.savePlayers(config.variant, config.players)
        return StartGameResult.Started(isPro)
    }
}
