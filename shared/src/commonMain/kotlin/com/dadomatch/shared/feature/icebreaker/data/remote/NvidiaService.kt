package com.dadomatch.shared.feature.icebreaker.data.remote

import com.dadomatch.shared.core.util.Resource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fast primary provider backed by NVIDIA NIM (https://integrate.api.nvidia.com),
 * which exposes an OpenAI-compatible `/chat/completions` endpoint. Any failure here
 * is surfaced as [Resource.Error] so [RoutingIcebreakerService] can fall back to Gemini.
 */
class NvidiaService(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val modelName: String,
    private val baseUrl: String,
) : IcebreakerAiService {

    override val providerId: String = "nvidia"

    override suspend fun generateIcebreaker(
        environment: String,
        intensity: String,
        language: String,
        usePremiumModel: Boolean,
    ): Resource<String> {
        // No key configured → let the router fall straight through to Gemini.
        if (apiKey.isBlank()) return Resource.Error("nvidia_not_configured")

        val prompt = IcebreakerPrompt.build(environment, intensity, language)
        val request = ChatRequest(
            model = modelName,
            messages = listOf(ChatMessage(role = "user", content = prompt)),
            // Lifetime users get a touch more creative variance; output stays one-liner short.
            temperature = if (usePremiumModel) 0.9 else 0.7,
            topP = 0.95,
            maxTokens = 160,
        )

        return try {
            val response: HttpResponse = httpClient.post("${baseUrl.trimEnd('/')}/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (!response.status.isSuccess()) {
                return when (response.status.value) {
                    429 -> Resource.Error("rate_limit_exceeded")
                    401, 403 -> Resource.Error("nvidia_auth_error")
                    else -> Resource.Error("ai_connection_error")
                }
            }

            val text = response.body<ChatResponse>().choices.firstOrNull()?.message?.content?.trim()
            if (text.isNullOrEmpty()) Resource.Error("ai_connection_error")
            else Resource.Success(text)
        } catch (e: Exception) {
            val msg = e.message ?: ""
            val networkLike = listOf(
                "timeout", "Unable to resolve host", "No address associated",
                "Failed to connect", "Network is unreachable",
                "UnknownHost", "ConnectException",
            ).any { msg.contains(it, ignoreCase = true) }
            if (networkLike) Resource.Error("no_internet", e)
            else Resource.Error(msg.ifEmpty { "ai_connection_error" }, e)
        }
    }
}

// ── OpenAI-compatible request/response DTOs ───────────────────────────────────
@Serializable
private data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double,
    @SerialName("top_p") val topP: Double,
    @SerialName("max_tokens") val maxTokens: Int,
)

@Serializable
private data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
private data class ChatResponse(
    val choices: List<Choice> = emptyList(),
)

@Serializable
private data class Choice(
    val message: ChatMessage,
)
