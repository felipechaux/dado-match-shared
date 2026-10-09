package com.dadomatch.shared.feature.game.domain

/** Fiesta = a group passing one phone around; Cita = two people, questions that go deeper. */
enum class GameVariant { PARTY, DATE }

enum class GameTone {
    FUNNY,
    ROMANTIC,
    SPICY;

    /** Free users only get [FUNNY]. */
    val requiresPro: Boolean get() = this != FUNNY
}

enum class ChallengeType { QUESTION, MOST_LIKELY, CONFESSION, DARE }

/** Date mode levels, unlocked as the rounds go by. */
enum class DateLevel { WARM_UP, DEEPER, SPICY }

/** Optional setting the AI writes the challenges for. */
enum class GameContext(val variant: GameVariant?) {
    NONE(null),
    BIRTHDAY(GameVariant.PARTY),
    PRE_PARTY(GameVariant.PARTY),
    TRIP(GameVariant.PARTY),
    FIRST_DATE(GameVariant.DATE),
    COUPLE(GameVariant.DATE),
    ONLINE_MATCH(GameVariant.DATE);

    companion object {
        fun forVariant(variant: GameVariant): List<GameContext> =
            entries.filter { it.variant == null || it.variant == variant }
    }
}

data class Challenge(val type: ChallengeType, val text: String)

data class GameConfig(
    val variant: GameVariant,
    val tone: GameTone,
    val context: GameContext,
    /** 3–8 names in party mode; 0 or 2 in date mode. */
    val players: List<String>,
    val language: String,
)

/** One revealed turn: who plays (null when nobody was named) and what they have to do. */
data class GameTurn(
    val round: Int,
    val player: String?,
    val challenge: Challenge,
    val level: DateLevel?,
)
