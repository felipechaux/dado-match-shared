package com.dadomatch.shared.data.remote

import com.dadomatch.shared.core.Resource
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.RequestOptions
import dev.shreyaspatil.ai.client.generativeai.type.generationConfig

class GeminiService(apiKey: String, modelName: String) {
    private val sanitizedModelName = modelName.trim().replace(" ", "-")
    
    private val model = GenerativeModel(
        modelName = sanitizedModelName,
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 100
        },
        requestOptions = RequestOptions(apiVersion = "v1")
    )

    suspend fun generateIcebreaker(environment: String, intensity: String, language: String): Resource<String> {
        val isSpanish = language.lowercase().contains("es")
        val langName = if (isSpanish) "Español" else "English"
        
        val systemPrompt = if (isSpanish) {
            "Eres Gemini, un experto en 'rizz' y dinámicas sociales."
        } else {
            "You are Gemini, an expert in 'rizz' and social dynamics."
        }

        val instructions = if (isSpanish) {
            """
            ## Instrucciones
            Genera un rompehielos (icebreaker) para una persona en un ambiente de '$environment' con una intensidad '$intensity'.
            Adaptas tu tono, energía y humor al estilo solicitado.
            
            ## Reglas
            - El tono debe ser natural, no robótico.
            - Si la intensidad es 'Romántico', sé sutil y encantador.
            - Si es 'Gracioso', usa humor inteligente.
            - Responde ÚNICAMENTE con la frase del rompehielos.
            - NO uses comillas ni etiquetas.
            - Idioma: $langName.
            """.trimIndent()
        } else {
            """
            ## Instructions
            Generate an icebreaker for a person in a '$environment' environment with '$intensity' intensity.
            Adapt your tone, energy, and humor to the requested style.
            
            ## Rules
            - The tone should be natural, not robotic.
            - If intensity is 'Romantic', be subtle and charming.
            - If it's 'Funny', use clever humor.
            - Respond ONLY with the icebreaker phrase.
            - NO quotes or labels.
            - Language: $langName.
            """.trimIndent()
        }

        val fullPrompt = "$systemPrompt\n\n$instructions"


        return try {
            val response = model.generateContent(fullPrompt)
            val result = response.text?.trim() ?: "fallback_icebreaker"
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "ai_connection_error", e)
        }
    }
}
