package com.dadomatch.shared.feature.game.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameSessionTest {

    private fun config(variant: GameVariant, players: List<String> = listOf("Ana", "Beto", "Caro")) =
        GameConfig(variant, GameTone.FUNNY, GameContext.NONE, players, "es")

    /** Hands out numbered challenges and records which levels were asked for. */
    private class FakeFetch(private val batchSize: Int = 4) {
        val levels = mutableListOf<DateLevel?>()
        private var next = 0
        suspend fun invoke(level: DateLevel?, avoid: List<String>): List<Challenge> {
            levels += level
            return List(batchSize) { Challenge(ChallengeType.QUESTION, "c${next++}") }
        }
    }

    @Test
    fun free_game_ends_after_the_round_cap() = runTest {
        val fetch = FakeFetch()
        val session = GameSession(config(GameVariant.PARTY), isPro = false, fetch = fetch::invoke)
        session.prepare()
        repeat(GameRules.FREE_ROUNDS_PER_GAME) { assertNotNull(session.nextTurn()) }
        assertFalse(session.hasNextTurn)
        assertNull(session.nextTurn())
    }

    @Test
    fun challenges_are_not_repeated() = runTest {
        val session = GameSession(config(GameVariant.PARTY), isPro = true, fetch = FakeFetch()::invoke)
        val texts = List(30) {
            session.prefetch()
            session.nextTurn()!!.challenge.text
        }
        assertEquals(texts.size, texts.toSet().size)
    }

    @Test
    fun date_game_asks_for_the_level_of_the_round() = runTest {
        val fetch = FakeFetch(batchSize = 5)
        val session = GameSession(config(GameVariant.DATE, players = emptyList()), isPro = true, fetch = fetch::invoke)
        session.prepare()
        val turns = List(11) { session.nextTurn()!! }
        assertEquals(DateLevel.WARM_UP, turns[4].level)
        assertEquals(DateLevel.DEEPER, turns[5].level)
        assertEquals(DateLevel.SPICY, turns[10].level)
        assertTrue(DateLevel.SPICY in fetch.levels)
        assertNull(turns[0].player)
    }

    @Test
    fun repeats_rather_than_stopping_when_the_deck_runs_out() = runTest {
        val sameDeck: suspend (DateLevel?, List<String>) -> List<Challenge> = { _, _ ->
            listOf(Challenge(ChallengeType.DARE, "only one"))
        }
        val session = GameSession(config(GameVariant.PARTY), isPro = true, fetch = sameDeck)
        repeat(3) { assertEquals("only one", session.nextTurn()?.challenge?.text) }
    }
}
