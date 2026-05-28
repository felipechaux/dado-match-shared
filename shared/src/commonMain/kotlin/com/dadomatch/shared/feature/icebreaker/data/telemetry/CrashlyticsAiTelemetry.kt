package com.dadomatch.shared.feature.icebreaker.data.telemetry

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.crashlytics.crashlytics

/**
 * Splits AI telemetry across the two Firebase tools that fit each signal:
 *  - **Analytics** (`ai_icebreaker` event) for performance — provider, latency, winner.
 *  - **Crashlytics** non-fatals for reliability — failures, fallbacks, total failure.
 *
 * Booleans are encoded as 1/0 longs so they aggregate cleanly as Analytics metrics.
 *
 * Every Firebase touch is wrapped in [runCatching]: if the consuming app hasn't
 * initialised Firebase yet (no `google-services.json` / `GoogleService-Info.plist`),
 * these calls become harmless no-ops instead of crashing generation.
 */
class CrashlyticsAiTelemetry : AiTelemetry {

    override fun onProviderAttempt(
        provider: String,
        success: Boolean,
        latencyMs: Long,
        premium: Boolean,
        fellBack: Boolean,
        errorCode: String?,
    ) {
        runCatching {
            val params = buildMap<String, Any> {
                put("provider", provider)
                put("success", if (success) 1L else 0L)
                put("latency_ms", latencyMs)
                put("premium", if (premium) 1L else 0L)
                put("fell_back", if (fellBack) 1L else 0L)
                errorCode?.let { put("error_code", it) }
            }
            Firebase.analytics.logEvent("ai_icebreaker", params)
        }
    }

    override fun onProviderFailure(
        provider: String,
        errorCode: String,
        fellBack: Boolean,
        cause: Throwable?,
    ) {
        runCatching {
            Firebase.crashlytics.apply {
                setCustomKey("ai_provider", provider)
                setCustomKey("ai_error_code", errorCode)
                setCustomKey("ai_fell_back", fellBack)
                log("AI provider '$provider' failed: $errorCode (fellBack=$fellBack)")
                recordException(cause ?: AiProviderException(provider, errorCode))
            }
        }
    }

    override fun onTotalFailure(primaryError: String, fallbackError: String) {
        runCatching {
            Firebase.crashlytics.apply {
                setCustomKey("ai_primary_error", primaryError)
                setCustomKey("ai_fallback_error", fallbackError)
                log("AI total failure: primary=$primaryError fallback=$fallbackError")
                recordException(AiTotalFailureException(primaryError, fallbackError))
            }
        }
    }
}

/** Synthetic throwable so a provider failure shows up as a distinct Crashlytics issue. */
class AiProviderException(provider: String, errorCode: String) :
    Exception("AI provider '$provider' failed: $errorCode")

/** Synthetic throwable for the worst case: every provider failed, user got nothing. */
class AiTotalFailureException(primaryError: String, fallbackError: String) :
    Exception("All AI providers failed (primary=$primaryError, fallback=$fallbackError)")
