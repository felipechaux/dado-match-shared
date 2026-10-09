package com.dadomatch.shared.feature.game.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameRulesTest {

    @Test
    fun party_needs_three_to_eight_players() {
        assertFalse(GameRules.canStart(GameVariant.PARTY, 2))
        assertTrue(GameRules.canStart(GameVariant.PARTY, 3))
        assertTrue(GameRules.canStart(GameVariant.PARTY, 8))
        assertFalse(GameRules.canAddPlayer(GameVariant.PARTY, 8))
    }

    @Test
    fun date_names_are_optional_but_come_in_pairs() {
        assertTrue(GameRules.canStart(GameVariant.DATE, 0))
        assertFalse(GameRules.canStart(GameVariant.DATE, 1))
        assertTrue(GameRules.canStart(GameVariant.DATE, 2))
        assertFalse(GameRules.canAddPlayer(GameVariant.DATE, 2))
    }

    @Test
    fun date_levels_rise_every_five_rounds() {
        assertEquals(DateLevel.WARM_UP, GameRules.levelForRound(1, spicyUnlocked = true))
        assertEquals(DateLevel.WARM_UP, GameRules.levelForRound(5, spicyUnlocked = true))
        assertEquals(DateLevel.DEEPER, GameRules.levelForRound(6, spicyUnlocked = true))
        assertEquals(DateLevel.DEEPER, GameRules.levelForRound(10, spicyUnlocked = true))
        assertEquals(DateLevel.SPICY, GameRules.levelForRound(11, spicyUnlocked = true))
    }

    @Test
    fun free_users_never_reach_the_spicy_level() {
        assertEquals(DateLevel.DEEPER, GameRules.levelForRound(11, spicyUnlocked = false))
        assertEquals(DateLevel.DEEPER, GameRules.levelForRound(40, spicyUnlocked = false))
    }

    @Test
    fun free_games_stop_after_the_round_cap() {
        assertTrue(GameRules.isRoundAllowed(GameRules.FREE_ROUNDS_PER_GAME, isPro = false))
        assertFalse(GameRules.isRoundAllowed(GameRules.FREE_ROUNDS_PER_GAME + 1, isPro = false))
        assertTrue(GameRules.isRoundAllowed(100, isPro = true))
    }

    @Test
    fun party_rotation_gives_everyone_a_turn_before_repeating() {
        val players = listOf("Ana", "Beto", "Caro", "Dani")
        val rotation = PlayerRotation(players, GameVariant.PARTY, Random(7))
        repeat(5) {
            val round = List(players.size) { rotation.next() }
            assertEquals(players.toSet(), round.toSet())
        }
    }

    @Test
    fun party_rotation_never_repeats_a_player_back_to_back() {
        val rotation = PlayerRotation(listOf("Ana", "Beto", "Caro"), GameVariant.PARTY, Random(1))
        var previous = rotation.next()
        repeat(300) {
            val current = rotation.next()
            assertNotEquals(previous, current)
            previous = current
        }
    }

    @Test
    fun date_rotation_alternates() {
        val rotation = PlayerRotation(listOf("Ana", "Beto"), GameVariant.DATE)
        assertEquals(listOf("Ana", "Beto", "Ana", "Beto"), List(4) { rotation.next() })
    }

    @Test
    fun nobody_named_means_no_player() {
        assertNull(PlayerRotation(emptyList(), GameVariant.DATE).next())
    }
}
