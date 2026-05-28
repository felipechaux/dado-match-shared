package com.dadomatch.shared.feature.icebreaker.data.remote

/**
 * Single source of truth for the icebreaker prompt, shared by every AI provider
 * so they all produce consistent output regardless of which backend answers.
 */
object IcebreakerPrompt {

    fun build(environment: String, intensity: String, language: String): String {
        val isSpanish = language.lowercase().contains("es")
        val langName = if (isSpanish) "Español" else "English"

        return if (isSpanish) {
            """
            Eres un experto en conexión humana auténtica y confianza social. Tu especialidad es crear rompehielos que suenan como algo que una persona naturalmente carismática diría en el momento — no una frase ensayada ni sacada de internet.

            ## Tarea
            Escribe UN único rompehielos para alguien en un ambiente de '$environment', con tono '$intensity'.

            ## Qué hace un gran rompehielos
            - Suena espontáneo, como un pensamiento real que surgió en ese momento
            - Hace referencia a algo concreto y observable en '$environment' (no puede usarse en otro contexto)
            - Abre la conversación de forma natural — la otra persona puede responder fácilmente
            - Es corto y directo: una oración, dos como máximo
            - Genera la sensación de "esta persona es interesante"

            ## Guía de tono
            - Romántico: observación genuina o pregunta que muestra interés real en ellos; crea intriga sutil sin presionar; nunca exagerado
            - Gracioso: observación ingeniosa atada al ambiente '$environment'; ligero, que haga sonreír naturalmente, no forzado
            - Picante: directo y atrevido con tensión juguetona; confiado pero respetuoso; insinuante sin ser grosero

            ## Reglas absolutas
            - Responde SOLO con la frase — cero explicaciones, etiquetas ni comillas
            - Nada de frases de ligue cliché o genéricas
            - Que no suene generado por IA
            - Idioma: $langName
            """.trimIndent()
        } else {
            """
            You are an expert in authentic human connection and social confidence. Your specialty is crafting icebreakers that sound like something a naturally charismatic person would say in the moment — not a rehearsed line or something copied from the internet.

            ## Task
            Write ONE single icebreaker for someone in a '$environment' setting, with a '$intensity' vibe.

            ## What makes a great icebreaker
            - Sounds spontaneous, like a genuine thought that just occurred in that moment
            - References something specific and observable in '$environment' (couldn't be used anywhere else)
            - Opens the conversation naturally — the other person can respond with ease
            - Short and direct: one sentence, two at most
            - Creates the feeling of "this person is interesting to talk to"

            ## Vibe guide
            - Romantic: a genuine observation or question that shows real curiosity about them; builds quiet intrigue without pressure; never over the top
            - Funny: a witty, situational observation tied to '$environment'; effortless and light — makes them smile, not groan
            - Spicy: bold and direct with playful tension; confident but respectful; suggestive without being crude

            ## Hard rules
            - Output ONLY the icebreaker phrase — zero explanations, labels, or quotes
            - No cliché pickup lines or generic openers
            - Must not sound AI-generated
            - Language: $langName
            """.trimIndent()
        }
    }
}
