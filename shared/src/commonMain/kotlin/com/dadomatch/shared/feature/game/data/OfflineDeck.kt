package com.dadomatch.shared.feature.game.data

import com.dadomatch.shared.feature.game.domain.Challenge
import com.dadomatch.shared.feature.game.domain.ChallengeType
import com.dadomatch.shared.feature.game.domain.ChallengeType.CONFESSION
import com.dadomatch.shared.feature.game.domain.ChallengeType.DARE
import com.dadomatch.shared.feature.game.domain.ChallengeType.MOST_LIKELY
import com.dadomatch.shared.feature.game.domain.ChallengeType.QUESTION
import com.dadomatch.shared.feature.game.domain.DateLevel
import com.dadomatch.shared.feature.game.domain.GameVariant

/**
 * Hand-written challenges for when the AI can't answer (no internet, both providers
 * down, Pro AI budget spent), so a game never stops halfway.
 */
object OfflineDeck {

    fun challenges(variant: GameVariant, level: DateLevel?, language: String): List<Challenge> {
        val spanish = language.lowercase().startsWith("es")
        return when (variant) {
            GameVariant.PARTY -> if (spanish) PARTY_ES else PARTY_EN
            GameVariant.DATE -> when (level ?: DateLevel.WARM_UP) {
                DateLevel.WARM_UP -> if (spanish) DATE_WARM_UP_ES else DATE_WARM_UP_EN
                DateLevel.DEEPER -> if (spanish) DATE_DEEPER_ES else DATE_DEEPER_EN
                DateLevel.SPICY -> if (spanish) DATE_SPICY_ES else DATE_SPICY_EN
            }
        }
    }

    private fun deck(type: ChallengeType, vararg texts: String) = texts.map { Challenge(type, it) }

    private val PARTY_ES =
        deck(
            QUESTION,
            "¿Cuál es la peor moda que seguiste y de la que hoy te avergüenzas?",
            "Si tu vida fuera una serie, ¿cómo se llamaría?",
            "¿Qué app borrarías para siempre si solo pudieras quedarte con tres?",
            "¿Cuál fue el regalo más raro que te han dado?",
            "¿Qué canción pondrías para entrar a un ring de boxeo?",
            "¿Qué harías con un día completo sin teléfono?",
        ) + deck(
            MOST_LIKELY,
            "¿Quién es más probable que se quede dormido en su propia fiesta?",
            "¿Quién es más probable que se haga famoso por un video viral?",
            "¿Quién es más probable que llegue tarde a su propia boda?",
            "¿Quién es más probable que adopte cinco gatos?",
            "¿Quién es más probable que le hable a una planta?",
            "¿Quién es más probable que gane un reality show?",
        ) + deck(
            CONFESSION,
            "Confiesa la excusa más absurda que hayas usado para cancelar un plan.",
            "Confiesa a quién del grupo le copiarías el estilo.",
            "Confiesa la última búsqueda rara que hiciste en internet.",
            "Confiesa una película que dices haber visto pero nunca viste.",
            "Confiesa tu placer culposo musical.",
            "Confiesa el peor corte de pelo que te has hecho.",
        ) + deck(
            DARE,
            "Imita a alguien del grupo hasta que adivinen quién es.",
            "Habla con acento extranjero durante tu próximo turno.",
            "Haz tu mejor pose de modelo durante 10 segundos.",
            "Cuenta un chiste malo; si nadie se ríe, cuenta otro.",
            "Canta el coro de la última canción que escuchaste.",
            "Describe tu día de hoy como si fuera un tráiler de película.",
        )

    private val PARTY_EN =
        deck(
            QUESTION,
            "What's the worst trend you followed that you're embarrassed about now?",
            "If your life were a TV show, what would it be called?",
            "If you could only keep three apps, which one would you delete first?",
            "What's the weirdest gift you've ever received?",
            "What song would you walk out to before a boxing match?",
            "What would you do with a whole day without your phone?",
        ) + deck(
            MOST_LIKELY,
            "Who's most likely to fall asleep at their own party?",
            "Who's most likely to go viral by accident?",
            "Who's most likely to be late to their own wedding?",
            "Who's most likely to adopt five cats?",
            "Who's most likely to talk to their plants?",
            "Who's most likely to win a reality show?",
        ) + deck(
            CONFESSION,
            "Confess the most ridiculous excuse you've used to cancel plans.",
            "Confess whose style in this group you'd steal.",
            "Confess the last weird thing you searched online.",
            "Confess a movie you say you've seen but never did.",
            "Confess your guilty-pleasure song.",
            "Confess the worst haircut you've ever had.",
        ) + deck(
            DARE,
            "Impersonate someone in the group until they guess who it is.",
            "Speak with a foreign accent until your next turn.",
            "Strike your best model pose for 10 seconds.",
            "Tell a bad joke; if nobody laughs, tell another one.",
            "Sing the chorus of the last song you listened to.",
            "Describe your day as if it were a movie trailer.",
        )

    private val DATE_WARM_UP_ES = deck(
        QUESTION,
        "¿Cuál es tu plan perfecto para un domingo?",
        "¿Qué comida podrías comer todos los días sin cansarte?",
        "¿Cuál es el mejor viaje que has hecho?",
        "¿Qué te hace reír sin falta?",
        "¿Qué talento oculto tienes?",
        "¿Eres más de playa o de montaña, y por qué?",
    ) + deck(
        DARE,
        "Muéstrale la última foto de tu galería y cuenta la historia.",
        "Pon una canción que te represente y explica por qué.",
    )

    private val DATE_WARM_UP_EN = deck(
        QUESTION,
        "What's your perfect Sunday?",
        "What food could you eat every day without getting tired of it?",
        "What's the best trip you've ever taken?",
        "What always makes you laugh?",
        "What's a hidden talent of yours?",
        "Beach or mountains, and why?",
    ) + deck(
        DARE,
        "Show them the last photo in your camera roll and tell the story.",
        "Play a song that represents you and explain why.",
    )

    private val DATE_DEEPER_ES = deck(
        QUESTION,
        "¿Qué es algo que cambió tu forma de ver la vida?",
        "¿De qué estás más orgulloso u orgullosa este año?",
        "¿Qué te gustaría que la gente supiera de ti sin tener que decirlo?",
        "¿Cuál es un sueño que todavía no le cuentas a casi nadie?",
        "¿Qué significa para ti sentirte en casa con alguien?",
        "¿Qué aprendiste de tu última relación?",
    ) + deck(
        DARE,
        "Dile algo que te haya gustado de esta conversación.",
        "Mírense a los ojos 30 segundos sin hablar.",
    )

    private val DATE_DEEPER_EN = deck(
        QUESTION,
        "What's something that changed the way you see life?",
        "What are you most proud of this year?",
        "What do you wish people knew about you without having to say it?",
        "What's a dream you've barely told anyone about?",
        "What does feeling at home with someone mean to you?",
        "What did you learn from your last relationship?",
    ) + deck(
        DARE,
        "Tell them something you've liked about this conversation.",
        "Look into each other's eyes for 30 seconds without talking.",
    )

    private val DATE_SPICY_ES = deck(
        QUESTION,
        "¿Qué fue lo primero que pensaste al verme?",
        "¿Cuál es tu idea de una cita perfecta que termine tarde?",
        "¿Qué detalle te parece irresistible en alguien?",
        "¿Cuál ha sido tu momento más atrevido en una cita?",
        "¿Qué te gustaría que pasara antes de que termine esta noche?",
        "¿Dónde sería el lugar perfecto para un primer beso?",
    ) + deck(
        DARE,
        "Susúrrale un cumplido al oído.",
        "Elige la canción para bailar juntos 30 segundos.",
    )

    private val DATE_SPICY_EN = deck(
        QUESTION,
        "What was the first thing you thought when you saw me?",
        "What's your idea of a perfect date that ends late?",
        "What little detail do you find irresistible in someone?",
        "What's the boldest thing you've done on a date?",
        "What would you like to happen before tonight ends?",
        "Where would the perfect first kiss happen?",
    ) + deck(
        DARE,
        "Whisper a compliment in their ear.",
        "Pick a song and dance together for 30 seconds.",
    )
}
