package com.dadomatch.shared.feature.icebreaker.data.telemetry

import com.dadomatch.shared.core.log.AppLogger
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
        }.onFailure {
            AppLogger.warn(TAG, "Analytics logEvent(ai_icebreaker) failed — is Firebase initialised?", it)
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
                // Surface the raw upstream message too so things like NVIDIA's
                // "method doesn't allow unregistered callers" land directly in the issue.
                cause?.message?.take(500)?.let { setCustomKey("ai_cause_message", it) }
                log("AI provider '$provider' failed: $errorCode (fellBack=$fellBack) cause=${cause?.message ?: "n/a"}")
                // Chain the original cause so its stack trace is preserved alongside
                // the readable summary in the synthetic exception.
                recordException(AiProviderException(provider, errorCode, cause))
            }
            AppLogger.debug(TAG, "Recorded provider failure → Crashlytics: provider=$provider code=$errorCode fellBack=$fellBack cause=${cause?.message ?: "n/a"}")
        }.onFailure {
            AppLogger.warn(TAG, "Failed to record provider failure to Crashlytics — is Firebase initialised?", it)
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
            AppLogger.debug(TAG, "Recorded total failure → Crashlytics: primary=$primaryError fallback=$fallbackError")
        }.onFailure {
            AppLogger.warn(TAG, "Failed to record total failure to Crashlytics — is Firebase initialised?", it)
        }
    }

    override fun onUnexpectedError(stage: String, cause: Throwable) {
        runCatching {
            Firebase.crashlytics.apply {
                setCustomKey("ai_stage", stage)
                cause.message?.take(500)?.let { setCustomKey("ai_cause_message", it) }
                log("AI unexpected error at stage=$stage: ${cause.message ?: cause::class.simpleName}")
                recordException(cause)
            }
            AppLogger.debug(TAG, "Recorded unexpected error → Crashlytics: stage=$stage cause=${cause.message ?: cause::class.simpleName}")
        }.onFailure {
            AppLogger.warn(TAG, "Failed to record unexpected error to Crashlytics — is Firebase initialised?", it)
        }
    }

    private companion object {
        const val TAG = "AiTelemetry"
    }
}

/** Synthetic throwable so a provider failure shows up as a distinct Crashlytics issue. */
class AiProviderException(provider: String, errorCode: String, cause: Throwable? = null) :
    Exception("AI provider '$provider' failed: $errorCode", cause)

/** Synthetic throwable for the worst case: every provider failed, user got nothing. */
class AiTotalFailureException(primaryError: String, fallbackError: String) :
    Exception("All AI providers failed (primary=$primaryError, fallback=$fallbackError)")
