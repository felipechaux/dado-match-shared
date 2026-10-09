package com.dadomatch.shared.feature.engagement.domain

/**
 * Texts of the scheduled reminders. Kept in code rather than strings.xml because
 * they are built outside the UI and must follow the in-app language, not the
 * system one.
 */
object NotificationCopy {

    data class Text(val title: String, val body: String)

    fun forKind(kind: NotificationKind, language: String, streakDays: Int = 0, sampleIndex: Int = 0): Text {
        val es = language.lowercase().startsWith("es")
        return when (kind) {
            NotificationKind.STREAK -> if (es) {
                Text("🔥 Racha de $streakDays días", "No la pierdas hoy: un lanzamiento y sigue viva.")
            } else {
                Text("🔥 $streakDays-day streak", "Don't lose it today — one roll keeps it alive.")
            }
            NotificationKind.ROLLS_REFILLED -> if (es) {
                Text("🎲 Tus rompehielos están listos", "Tus 3 rompehielos gratis de hoy ya están disponibles.")
            } else {
                Text("🎲 Your icebreakers are ready", "Your 3 free icebreakers for today are available.")
            }
            NotificationKind.NIGHT_PLAN -> if (es) {
                Text("¿Sales hoy? 😏", "Lleva un rompehielos preparado — tira el dado antes de salir.")
            } else {
                Text("Going out tonight? 😏", "Bring an icebreaker ready — roll the dice before you go.")
            }
            NotificationKind.MISS_YOU -> {
                val samples = if (es) SAMPLES_ES else SAMPLES_EN
                val sample = samples[sampleIndex.mod(samples.size)]
                if (es) Text("Prueba este 👇", "«$sample» — tira el dado por más.")
                else Text("Try this one 👇", "“$sample” — roll the dice for more.")
            }
        }
    }

    private val SAMPLES_ES = listOf(
        "¿Ese café se ve tan bueno como huele o me estás engañando?",
        "Necesito una opinión neutral: ¿esta canción es buenísima o solo yo la estoy disfrutando?",
        "Te vi dudar con el menú igual que yo — ¿qué me recomiendas?",
    )

    private val SAMPLES_EN = listOf(
        "Is that coffee as good as it smells, or are you just good at pretending?",
        "I need a neutral opinion: is this song great, or is it just me?",
        "You looked at that menu the way I did — what would you recommend?",
    )
}
