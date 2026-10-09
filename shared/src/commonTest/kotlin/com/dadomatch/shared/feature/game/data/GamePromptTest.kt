package com.dadomatch.shared.feature.game.data

import com.dadomatch.shared.feature.game.domain.Challenge
import com.dadomatch.shared.feature.game.domain.ChallengeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GamePromptTest {

    @Test
    fun parses_a_plain_array() {
        val raw = """[{"type":"question","text":"¿Qué harías?"},{"type":"dare","text":"Baila"}]"""
        assertEquals(
            listOf(Challenge(ChallengeType.QUESTION, "¿Qué harías?"), Challenge(ChallengeType.DARE, "Baila")),
            GamePrompt.parse(raw)
        )
    }

    @Test
    fun ignores_code_fences_and_chatter() {
        val raw = "Here you go:\n```json\n[{\"type\":\"most_likely\",\"text\":\"Who's most likely to sing?\"}]\n```"
        assertEquals(listOf(Challenge(ChallengeType.MOST_LIKELY, "Who's most likely to sing?")), GamePrompt.parse(raw))
    }

    @Test
    fun skips_malformed_items() {
        val raw = """[{"type":"poem","text":"x"},{"type":"confession"},"text",{"type":"CONFESSION","text":"  Confiesa algo  "}]"""
        assertEquals(listOf(Challenge(ChallengeType.CONFESSION, "Confiesa algo")), GamePrompt.parse(raw))
    }

    @Test
    fun unusable_replies_give_nothing() {
        assertTrue(GamePrompt.parse("Sorry, I can't help with that.").isEmpty())
        assertTrue(GamePrompt.parse("[not json").isEmpty())
    }
}
