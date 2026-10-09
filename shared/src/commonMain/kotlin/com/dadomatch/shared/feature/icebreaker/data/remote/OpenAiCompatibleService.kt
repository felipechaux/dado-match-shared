package com.dadomatch.shared.feature.icebreaker.data.remote

import com.dadomatch.shared.core.util.Resource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Icebreaker provider for any OpenAI-compatible `/chat/completions` endpoint — used for
 * both NVIDIA NIM and Gemini (via its OpenAI compatibility layer). Any failure is
 * surfaced as [Resource.Error] so [RoutingIcebreakerService] can race the other provider.
 *
 * Reasoning models must answer without "thinking": it adds seconds and, on some models,
 * leaks into the reply. Providers disable it differently, hence [thinkingOff].
 */
class OpenAiCompatibleService(
    private val httpClient: HttpClient,
    override val providerId: String,
    private val apiKey: String,
    private val baseUrl: String,
    private val modelName: String,
    private val premiumModelName: String = modelName,
    private val thinkingOff: ThinkingOff,
) : IcebreakerAiService {

    /** How a provider is told not to reason before answering. */
    enum class ThinkingOff {
        /** NVIDIA NIM: `chat_template_kwargs.enable_thinking = false`. */
        CHAT_TEMPLATE_KWARGS,

        /** Gemini: `reasoning_effort = "minimal"`. */
        MINIMAL_REASONING_EFFORT,
    }

    override suspend fun generateIcebreaker(
        environment: String,
        intensity: String,
        language: String,
        usePremiumModel: Boolean,
    ): Resource<String> {
        val reply = complete(IcebreakerPrompt.build(environment, intensity, language), ICEBREAKER_MAX_TOKENS, usePremiumModel)
        if (reply !is Resource.Success) return reply
        val text = IcebreakerPrompt.clean(reply.data)
        return if (text.isEmpty()) Resource.Error("ai_empty_response") else Resource.Success(text)
    }

    override suspend fun complete(prompt: String, maxTokens: Int, usePremiumModel: Boolean): Resource<String> {
        // No key configured → let the router fall straight through to the other provider.
        if (apiKey.isBlank()) return Resource.Error("${providerId}_not_configured")

        if (!usePremiumModel || premiumModelName == modelName) {
            return request(modelName, prompt, maxTokens, usePremiumModel)
        }

        // The premium model has tighter quotas: when it is saturated, lifetime users
        // silently get the standard model instead of an error.
        val premium = request(premiumModelName, prompt, maxTokens, usePremiumModel = true)
        return if (premium is Resource.Error && premium.message == RATE_LIMITED) {
            request(modelName, prompt, maxTokens, usePremiumModel = true)
        } else {
            premium
        }
    }

    private suspend fun request(model: String, prompt: String, maxTokens: Int, usePremiumModel: Boolean): Resource<String> {
        val request = ChatRequest(
            model = model,
            messages = listOf(ChatMessage(role = "user", content = prompt)),
            // Lifetime users get a touch more creative variance.
            temperature = if (usePremiumModel) 0.9 else 0.7,
            topP = 0.95,
            maxTokens = maxTokens,
            chatTemplateKwargs = ChatTemplateKwargs(enableThinking = false)
                .takeIf { thinkingOff == ThinkingOff.CHAT_TEMPLATE_KWARGS },
            reasoningEffort = "minimal".takeIf { thinkingOff == ThinkingOff.MINIMAL_REASONING_EFFORT },
        )

        return try {
            val response: HttpResponse = httpClient.post("${baseUrl.trimEnd('/')}/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (!response.status.isSuccess()) {
                // Read the body so the upstream error text (e.g. "method doesn't allow
                // unregistered callers...") is preserved for Crashlytics instead of
                // being collapsed to an opaque code.
                val status = response.status.value
                val body = runCatching { response.bodyAsText() }.getOrNull().orEmpty().take(500)
                val cause = AiHttpException(providerId, status, body)
                val code = when (status) {
                    // Overload ("high demand") is as temporary as a rate limit: same
                    // "AI is busy, retry" message for the user.
                    429, 503 -> RATE_LIMITED
                    401, 403 -> "${providerId}_auth_error"
                    else -> "${providerId}_http_$status"
                }
                return Resource.Error(code, cause)
            }

            val text = response.body<ChatResponse>().choices.firstOrNull()?.message?.content?.trim()
            if (text.isNullOrEmpty()) Resource.Error("ai_empty_response")
            else Resource.Success(text)
        } catch (e: CancellationException) {
            // Honor structured concurrency — never swallow cancellation as a fake error.
            throw e
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

    private companion object {
        const val RATE_LIMITED = "rate_limit_exceeded"
        // Output stays one-liner short
        const val ICEBREAKER_MAX_TOKENS = 160
    }
}

/** Synthetic throwable carrying the raw provider HTTP status + body for Crashlytics. */
class AiHttpException(val provider: String, val status: Int, val body: String) :
    Exception("$provider HTTP $status: ${body.ifBlank { "<empty body>" }}")

// ── OpenAI-compatible request/response DTOs ───────────────────────────────────
// Null fields are left out of the request (encodeDefaults is off), so each provider
// only receives the thinking switch it understands.
@Serializable
private data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double,
    @SerialName("top_p") val topP: Double,
    @SerialName("max_tokens") val maxTokens: Int,
    @SerialName("chat_template_kwargs") val chatTemplateKwargs: ChatTemplateKwargs? = null,
    @SerialName("reasoning_effort") val reasoningEffort: String? = null,
)

@Serializable
private data class ChatTemplateKwargs(
    @SerialName("enable_thinking") val enableThinking: Boolean,
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
