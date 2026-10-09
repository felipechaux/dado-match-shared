package com.dadomatch.shared.feature.game.domain.repository

import com.dadomatch.shared.feature.game.domain.Challenge
import com.dadomatch.shared.feature.game.domain.DateLevel
import com.dadomatch.shared.feature.game.domain.GameConfig
import com.dadomatch.shared.feature.game.domain.GameVariant
import kotlinx.datetime.LocalDate

interface GameRepository {
    /** A batch written by the AI, or null when it couldn't answer or the reply was unusable. */
    suspend fun generateChallenges(
        config: GameConfig,
        level: DateLevel?,
        avoid: List<String>,
        usePremiumModel: Boolean,
    ): List<Challenge>?

    fun offlineChallenges(config: GameConfig, level: DateLevel?): List<Challenge>

    suspend fun savedPlayers(variant: GameVariant): List<String>

    suspend fun savePlayers(variant: GameVariant, players: List<String>)

    suspend fun gamesStartedOn(day: LocalDate): Int

    suspend fun recordGameStarted(day: LocalDate)
}
