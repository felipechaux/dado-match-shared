package com.dadomatch.shared.feature.icebreaker.data.remote

import com.dadomatch.shared.core.util.Resource
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.RequestOptions
import dev.shreyaspatil.ai.client.generativeai.type.generationConfig

class GeminiService(private val apiKey: String, modelName: String, premiumModelName: String) {
    private val sanitizedModelName = modelName.trim().replace(" ", "-")
    private val sanitizedPremiumModelName = premiumModelName.trim().replace(" ", "-")

    private fun buildModel(name: String, maxOutputTokens: Int, temperature: Float) = GenerativeModel(
        modelName = name,
        apiKey = apiKey,
        generationConfig = generationConfig {
            this.temperature = temperature
            topK = 40
            topP = 0.95f
            this.maxOutputTokens = maxOutputTokens
        },
        requestOptions = RequestOptions(apiVersion = "v1")
    )

    // Standard model: conservative temperature for consistent short outputs
    private val defaultModel by lazy { buildModel(sanitizedModelName, maxOutputTokens = 200, temperature = 0.7f) }
    // Lifetime model: higher token budget + more creative temperature for richer outputs
    private val premiumModel by lazy { buildModel(sanitizedPremiumModelName, maxOutputTokens = 2048, temperature = 0.9f) }

    private fun isRateLimitError(msg: String, e: Exception) =
        msg.contains("quota", ignoreCase = true) ||
        msg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
        msg.contains("429")

    private fun isNetworkError(msg: String, e: Exception) =
        msg.contains("Unable to resolve host", ignoreCase = true) ||
        msg.contains("No address associated", ignoreCase = true) ||
        msg.contains("Failed to connect", ignoreCase = true) ||
        msg.contains("Network is unreachable", ignoreCase = true) ||
        msg.contains("timeout", ignoreCase = true) ||
        msg.contains("SocketTimeoutException", ignoreCase = true) ||
        msg.contains("UnknownHostException", ignoreCase = true) ||
        msg.contains("ConnectException", ignoreCase = true) ||
        e.cause?.let {
            it::class.simpleName?.contains("UnknownHost") == true ||
            it::class.simpleName?.contains("Connect") == true
        } == true

    suspend fun generateIcebreaker(
        environment: String,
        intensity: String,
        language: String,
        usePremiumModel: Boolean = false
    ): Resource<String> {
        val model = if (usePremiumModel) premiumModel else defaultModel
        val isSpanish = language.lowercase().contains("es")
        val langName = if (isSpanish) "Español" else "English"

        val fullPrompt = if (isSpanish) {
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


        return try {
            val response = model.generateContent(fullPrompt)
            Resource.Success(response.text?.trim() ?: "fallback_icebreaker")
        } catch (e: Exception) {
            val msg = e.message ?: ""
            when {
                isRateLimitError(msg, e) -> {
                    // Flash (5 RPM) is saturated — silently fall back to Flash Lite for lifetime users
                    if (usePremiumModel) {
                        try {
                            val fallbackResponse = defaultModel.generateContent(fullPrompt)
                            Resource.Success(fallbackResponse.text?.trim() ?: "fallback_icebreaker")
                        } catch (fallbackEx: Exception) {
                            val fallbackMsg = fallbackEx.message ?: ""
                            if (isRateLimitError(fallbackMsg, fallbackEx))
                                Resource.Error("rate_limit_exceeded", fallbackEx)
                            else if (isNetworkError(fallbackMsg, fallbackEx))
                                Resource.Error("no_internet", fallbackEx)
                            else
                                Resource.Error(fallbackMsg.ifEmpty { "ai_connection_error" }, fallbackEx)
                        }
                    } else {
                        Resource.Error("rate_limit_exceeded", e)
                    }
                }
                isNetworkError(msg, e) -> Resource.Error("no_internet", e)
                else -> Resource.Error(msg.ifEmpty { "ai_connection_error" }, e)
            }
        }
    }
}
