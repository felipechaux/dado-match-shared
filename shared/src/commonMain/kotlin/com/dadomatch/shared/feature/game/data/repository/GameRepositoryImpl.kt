package com.dadomatch.shared.feature.game.data.repository

import com.dadomatch.shared.core.util.Resource
import com.dadomatch.shared.feature.game.data.GamePrompt
import com.dadomatch.shared.feature.game.data.OfflineDeck
import com.dadomatch.shared.feature.game.data.local.GameLocalDataSource
import com.dadomatch.shared.feature.game.domain.Challenge
import com.dadomatch.shared.feature.game.domain.DateLevel
import com.dadomatch.shared.feature.game.domain.GameConfig
import com.dadomatch.shared.feature.game.domain.GameVariant
import com.dadomatch.shared.feature.game.domain.repository.GameRepository
import com.dadomatch.shared.feature.icebreaker.data.remote.IcebreakerAiService
import kotlinx.datetime.LocalDate

class GameRepositoryImpl(
    private val aiService: IcebreakerAiService,
    private val local: GameLocalDataSource,
) : GameRepository {

    override suspend fun generateChallenges(
        config: GameConfig,
        level: DateLevel?,
        avoid: List<String>,
        usePremiumModel: Boolean,
    ): List<Challenge>? {
        val reply = aiService.complete(GamePrompt.build(config, level, avoid), GamePrompt.MAX_TOKENS, usePremiumModel)
        return (reply as? Resource.Success)?.data?.let(GamePrompt::parse)?.takeIf { it.isNotEmpty() }
    }

    override fun offlineChallenges(config: GameConfig, level: DateLevel?): List<Challenge> =
        OfflineDeck.challenges(config.variant, level, config.language)

    override suspend fun savedPlayers(variant: GameVariant): List<String> = local.players(variant)

    override suspend fun savePlayers(variant: GameVariant, players: List<String>) = local.setPlayers(variant, players)

    override suspend fun gamesStartedOn(day: LocalDate): Int = local.gamesStartedOn(day)

    override suspend fun recordGameStarted(day: LocalDate) = local.recordGameStarted(day)
}
