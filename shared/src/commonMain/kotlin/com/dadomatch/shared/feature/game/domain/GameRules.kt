package com.dadomatch.shared.feature.game.domain

import kotlin.random.Random

/** The game's rules, kept pure so they are easy to read and to test. */
object GameRules {
    const val MIN_PARTY_PLAYERS = 3
    const val MAX_PARTY_PLAYERS = 8
    const val DATE_PLAYERS = 2
    const val MAX_NAME_LENGTH = 20

    const val FREE_ROUNDS_PER_GAME = 15
    const val FREE_GAMES_PER_DAY = 1

    /** Date mode goes one level deeper every this many rounds. */
    const val ROUNDS_PER_LEVEL = 5

    fun canStart(variant: GameVariant, playerCount: Int): Boolean = when (variant) {
        GameVariant.PARTY -> playerCount in MIN_PARTY_PLAYERS..MAX_PARTY_PLAYERS
        GameVariant.DATE -> playerCount == 0 || playerCount == DATE_PLAYERS
    }

    fun canAddPlayer(variant: GameVariant, playerCount: Int): Boolean = when (variant) {
        GameVariant.PARTY -> playerCount < MAX_PARTY_PLAYERS
        GameVariant.DATE -> playerCount < DATE_PLAYERS
    }

    /** The spicy level is Pro; free users stay on [DateLevel.DEEPER] after round 10. */
    fun levelForRound(round: Int, spicyUnlocked: Boolean): DateLevel = when {
        round <= ROUNDS_PER_LEVEL -> DateLevel.WARM_UP
        round <= ROUNDS_PER_LEVEL * 2 || !spicyUnlocked -> DateLevel.DEEPER
        else -> DateLevel.SPICY
    }

    fun isRoundAllowed(round: Int, isPro: Boolean): Boolean = isPro || round <= FREE_ROUNDS_PER_GAME
}

/**
 * Who plays next. Party mode deals from a shuffled bag so everyone plays once before
 * anyone plays twice, without the same person twice in a row across bags; date mode
 * simply alternates.
 */
class PlayerRotation(
    private val players: List<String>,
    private val variant: GameVariant,
    private val random: Random = Random.Default,
) {
    private val bag = ArrayDeque<String>()
    private var last: String? = null
    private var turns = 0

    fun next(): String? {
        if (players.isEmpty()) return null
        val player = when (variant) {
            GameVariant.DATE -> players[turns % players.size]
            GameVariant.PARTY -> {
                if (bag.isEmpty()) refill()
                bag.removeFirst()
            }
        }
        turns++
        last = player
        return player
    }

    private fun refill() {
        val shuffled = players.shuffled(random).toMutableList()
        if (shuffled.size > 1 && shuffled.first() == last) {
            shuffled.add(shuffled.removeAt(0))
        }
        bag.addAll(shuffled)
    }
}
