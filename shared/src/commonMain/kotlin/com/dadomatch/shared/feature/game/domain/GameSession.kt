package com.dadomatch.shared.feature.game.domain

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

/**
 * One game in progress: deals the turns and keeps a queue of challenges per level,
 * refilled through [fetch] ahead of time so a turn rarely waits on the network.
 */
class GameSession(
    val config: GameConfig,
    private val isPro: Boolean,
    private val fetch: suspend (level: DateLevel?, avoid: List<String>) -> List<Challenge>,
    random: Random = Random.Default,
) {
    private val mutex = Mutex()
    private val queues = mutableMapOf<DateLevel?, ArrayDeque<Challenge>>()
    private val seen = mutableListOf<String>()
    private val rotation = PlayerRotation(config.players, config.variant, random)

    var round = 0
        private set

    /** False once a free game has used all its rounds. */
    val hasNextTurn: Boolean get() = GameRules.isRoundAllowed(round + 1, isPro)

    /** Fills the first level's queue; call before the first turn. */
    suspend fun prepare() = refillIfLow(levelFor(1))

    /** The next turn, or null when a free game has run out of rounds. */
    suspend fun nextTurn(): GameTurn? {
        if (!hasNextTurn) return null
        val nextRound = round + 1
        val level = levelFor(nextRound)
        val challenge = mutex.withLock {
            val queue = queues.getOrPut(level) { ArrayDeque() }
            if (queue.isEmpty()) queue.addAll(fresh(level))
            queue.removeFirstOrNull()
        } ?: return null
        round = nextRound
        seen += challenge.text
        return GameTurn(round = round, player = rotation.next(), challenge = challenge, level = level)
    }

    /** Fetches the upcoming level's next batch in the background when few are left. */
    suspend fun prefetch() {
        if (hasNextTurn) refillIfLow(levelFor(round + 1))
    }

    private suspend fun refillIfLow(level: DateLevel?) = mutex.withLock {
        val queue = queues.getOrPut(level) { ArrayDeque() }
        if (queue.size < REFILL_BELOW) queue.addAll(fresh(level))
    }

    private suspend fun fresh(level: DateLevel?): List<Challenge> {
        val queued = queues.values.flatten().map { it.text }.toSet()
        val batch = fetch(level, seen.toList())
        // Repeats beat stopping the game when everything on offer was already played
        return batch.filter { it.text !in seen && it.text !in queued }.ifEmpty { batch }
    }

    private fun levelFor(round: Int): DateLevel? = when (config.variant) {
        GameVariant.PARTY -> null
        GameVariant.DATE -> GameRules.levelForRound(round, spicyUnlocked = isPro)
    }

    private companion object {
        const val REFILL_BELOW = 3
    }
}
