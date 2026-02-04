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

    suspend fun generateIcebreaker(environment: String, intensity: String): Resource<String> {
        val fullPrompt = """
            Eres Gemini, un experto en 'rizz' y dinámicas sociales.
            
            ## Instrucciones
            Genera un rompehielos (icebreaker) para una persona en un ambiente de '$environment' con una intensidad '$intensity'.
            Adaptas tu tono, energía y humor al estilo solicitado.
            
            ## Reglas
            - El tono debe ser natural, no robótico.
            - Si la intensidad es 'Romántico', sé sutil y encantador.
            - Si es 'Gracioso', usa humor inteligente.
            - Responde ÚNICAMENTE con la frase del rompehielos.
            - NO uses comillas ni etiquetas.
            - Idioma: Español.
        """.trimIndent()

        return try {
            val response = model.generateContent(fullPrompt)
            val result = response.text?.trim() ?: "¡Lánzate y dile hola!"
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error("Error al conectar con la IA: ${e.message}", e)
        }
    }
}
