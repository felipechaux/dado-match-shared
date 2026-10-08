package com.dadomatch.shared.feature.icebreaker.data.remote

import com.dadomatch.shared.core.util.Resource
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.RequestOptions
import dev.shreyaspatil.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.CancellationException

class GeminiService(
    private val apiKey: String,
    modelName: String,
    premiumModelName: String,
) : IcebreakerAiService {
    override val providerId: String = "gemini"

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

    // Overload (503 "high demand") is treated like a rate limit: it is just as
    // temporary and the user gets the same "AI is busy, retry" message.
    private fun isRateLimitError(msg: String, e: Exception) =
        msg.contains("quota", ignoreCase = true) ||
        msg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
        msg.contains("429") ||
        msg.contains("503") ||
        msg.contains("UNAVAILABLE", ignoreCase = true) ||
        msg.contains("overloaded", ignoreCase = true) ||
        msg.contains("high demand", ignoreCase = true)

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

    override suspend fun generateIcebreaker(
        environment: String,
        intensity: String,
        language: String,
        usePremiumModel: Boolean
    ): Resource<String> {
        val model = if (usePremiumModel) premiumModel else defaultModel
        val fullPrompt = IcebreakerPrompt.build(environment, intensity, language)

        return try {
            val response = model.generateContent(fullPrompt)
            Resource.Success(response.text?.let(IcebreakerPrompt::clean) ?: "fallback_icebreaker")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val msg = e.message ?: ""
            when {
                isRateLimitError(msg, e) -> {
                    // Flash (5 RPM) is saturated — silently fall back to Flash Lite for lifetime users
                    if (usePremiumModel) {
                        try {
                            val fallbackResponse = defaultModel.generateContent(fullPrompt)
                            Resource.Success(fallbackResponse.text?.let(IcebreakerPrompt::clean) ?: "fallback_icebreaker")
                        } catch (fallbackEx: CancellationException) {
                            throw fallbackEx
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
