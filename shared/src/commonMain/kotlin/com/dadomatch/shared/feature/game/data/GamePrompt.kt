package com.dadomatch.shared.feature.game.data

import com.dadomatch.shared.feature.game.domain.Challenge
import com.dadomatch.shared.feature.game.domain.ChallengeType
import com.dadomatch.shared.feature.game.domain.DateLevel
import com.dadomatch.shared.feature.game.domain.GameConfig
import com.dadomatch.shared.feature.game.domain.GameContext
import com.dadomatch.shared.feature.game.domain.GameTone
import com.dadomatch.shared.feature.game.domain.GameVariant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Prompt for a batch of game challenges, and the parser for the reply. One call
 * returns a whole batch so turns don't wait on the network.
 */
object GamePrompt {

    const val BATCH_SIZE = 8
    const val MAX_TOKENS = 900

    fun build(config: GameConfig, level: DateLevel?, avoid: List<String>): String {
        val spanish = config.language.lowercase().startsWith("es")
        val types = when (config.variant) {
            GameVariant.PARTY -> "question, most_likely, confession, dare"
            GameVariant.DATE -> "question, dare"
        }
        val avoidBlock = if (avoid.isEmpty()) "" else {
            val header = if (spanish) "No repitas ni parafrasees estos:" else "Don't repeat or paraphrase these:"
            "\n$header\n" + avoid.takeLast(MAX_AVOID).joinToString("\n") { "- $it" }
        }

        return if (spanish) {
            """
            Eres el anfitrión de un juego social para la app DadoMatch. Escribe $BATCH_SIZE retos ${describeGame(config, level, true)}.
            Tono: ${describeTone(config.tone, true)}.${describeContext(config.context, true)}

            Tipos permitidos ("type"): $types
            - question: una pregunta para quien tiene el turno
            - most_likely: "¿Quién es más probable que…?" — todos señalan a alguien a la cuenta de tres
            - confession: algo que quien tiene el turno debe confesar
            - dare: un mini reto que se hace ahí mismo, en menos de un minuto, sin salir del lugar
            Mezcla los tipos.

            Reglas:
            - Cada reto en una frase corta, que se entienda al leerla en voz alta
            - Nada peligroso, ilegal, humillante ni que obligue a gastar dinero
            - Nada que involucre a gente fuera del juego: no mandar mensajes, audios ni publicar o llamar a nadie
            - Escribe en español neutro, sin nombres de personas
            - Responde SOLO con un array JSON, sin texto antes ni después:
              [{"type":"question","text":"..."}]$avoidBlock
            """.trimIndent()
        } else {
            """
            You host a social game for the DadoMatch app. Write $BATCH_SIZE challenges ${describeGame(config, level, false)}.
            Vibe: ${describeTone(config.tone, false)}.${describeContext(config.context, false)}

            Allowed types ("type"): $types
            - question: a question for whoever's turn it is
            - most_likely: "Who's most likely to…?" — everyone points at someone on three
            - confession: something the current player has to confess
            - dare: a mini dare done right there in under a minute, without leaving the place
            Mix the types.

            Rules:
            - Each challenge is one short sentence that works read out loud
            - Nothing dangerous, illegal, humiliating, or that costs money
            - Nothing involving people outside the game: no texting, voice notes, posting or calling anyone
            - No names of people
            - Reply ONLY with a JSON array, no text before or after:
              [{"type":"question","text":"..."}]$avoidBlock
            """.trimIndent()
        }
    }

    /** Lenient: drops code fences or chatter around the array and skips malformed items. */
    fun parse(raw: String): List<Challenge> {
        val start = raw.indexOf('[')
        val end = raw.lastIndexOf(']')
        if (start == -1 || end <= start) return emptyList()
        val array = runCatching { json.parseToJsonElement(raw.substring(start, end + 1)) as? JsonArray }
            .getOrNull() ?: return emptyList()
        return array.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            val type = obj["type"]?.jsonPrimitive?.contentOrNull?.let(::parseType) ?: return@mapNotNull null
            val text = obj["text"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }
                ?: return@mapNotNull null
            Challenge(type, text)
        }
    }

    private fun parseType(value: String): ChallengeType? = when (value.trim().lowercase()) {
        "question" -> ChallengeType.QUESTION
        "most_likely" -> ChallengeType.MOST_LIKELY
        "confession" -> ChallengeType.CONFESSION
        "dare" -> ChallengeType.DARE
        else -> null
    }

    private fun describeGame(config: GameConfig, level: DateLevel?, spanish: Boolean): String = when (config.variant) {
        GameVariant.PARTY ->
            if (spanish) "para un grupo de amigos que se pasan un teléfono"
            else "for a group of friends passing one phone around"
        GameVariant.DATE -> {
            val levelText = when (level ?: DateLevel.WARM_UP) {
                DateLevel.WARM_UP -> if (spanish) "para conocerse, ligeras" else "getting to know each other, light"
                DateLevel.DEEPER -> if (spanish) "más profundas y personales" else "deeper and more personal"
                DateLevel.SPICY -> if (spanish) "con tensión y coqueteo atrevido, sin ser explícitas" else "flirty and daring, never explicit"
            }
            if (spanish) "para dos personas en una cita, $levelText" else "for two people on a date, $levelText"
        }
    }

    private fun describeTone(tone: GameTone, spanish: Boolean): String = when (tone) {
        GameTone.FUNNY -> if (spanish) "gracioso, para reírse juntos" else "funny, made to laugh together"
        GameTone.ROMANTIC -> if (spanish) "romántico, cálido y genuino" else "romantic, warm and genuine"
        GameTone.SPICY -> if (spanish) "picante y atrevido, pero respetuoso y nunca explícito" else "spicy and daring, but respectful and never explicit"
    }

    private fun describeContext(context: GameContext, spanish: Boolean): String {
        val text = when (context) {
            GameContext.NONE -> return ""
            GameContext.BIRTHDAY -> if (spanish) "una fiesta de cumpleaños" else "a birthday party"
            GameContext.PRE_PARTY -> if (spanish) "una previa antes de salir" else "pre-drinks before going out"
            GameContext.TRIP -> if (spanish) "un viaje entre amigos" else "a trip with friends"
            GameContext.FIRST_DATE -> if (spanish) "una primera cita" else "a first date"
            GameContext.COUPLE -> if (spanish) "una pareja que ya lleva tiempo junta" else "a couple who've been together a while"
            GameContext.ONLINE_MATCH -> if (spanish) "dos personas que hicieron match en una app" else "two people who matched on an app"
        }
        return if (spanish) "\nContexto: $text." else "\nSetting: $text."
    }

    private const val MAX_AVOID = 20
    private val json = Json { isLenient = true; ignoreUnknownKeys = true }
}
