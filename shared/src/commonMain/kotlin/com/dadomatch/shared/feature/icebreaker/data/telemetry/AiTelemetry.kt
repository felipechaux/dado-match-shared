package com.dadomatch.shared.feature.icebreaker.data.telemetry

/**
 * Observability hook for AI icebreaker generation.
 *
 * The two concerns are deliberately split (see [CrashlyticsAiTelemetry]):
 *  - **Performance** — per-provider latency and which provider answered — is meant
 *    for Firebase Analytics, so it can be aggregated into dashboards.
 *  - **Reliability** — provider failures, fallbacks, and total failures — is meant
 *    for Crashlytics non-fatals, so error patterns surface in the issues list.
 *
 * Every method MUST be best-effort and never throw: telemetry can never be allowed
 * to break icebreaker generation. Implementations should swallow their own errors.
 */
interface AiTelemetry {

    /**
     * One provider attempt finished (success or failure). Intended as an Analytics
     * event so latency and win-rate per provider can be charted.
     *
     * @param provider provider id, e.g. "nvidia" / "gemini"
     * @param fellBack true when this attempt was the fallback (i.e. primary already failed)
     * @param errorCode the [com.dadomatch.shared.core.util.Resource.Error] message when !success
     */
    fun onProviderAttempt(
        provider: String,
        success: Boolean,
        latencyMs: Long,
        premium: Boolean,
        fellBack: Boolean,
        errorCode: String? = null,
    )

    /** A provider failed — recorded as a Crashlytics non-fatal for error-pattern analysis. */
    fun onProviderFailure(
        provider: String,
        errorCode: String,
        fellBack: Boolean,
        cause: Throwable? = null,
    )

    /** Both providers failed; the user got no icebreaker. High-signal non-fatal. */
    fun onTotalFailure(primaryError: String, fallbackError: String)
}

/** Default sink used until Firebase is wired in the consuming apps. Does nothing. */
object NoOpAiTelemetry : AiTelemetry {
    override fun onProviderAttempt(
        provider: String,
        success: Boolean,
        latencyMs: Long,
        premium: Boolean,
        fellBack: Boolean,
        errorCode: String?,
    ) = Unit

    override fun onProviderFailure(
        provider: String,
        errorCode: String,
        fellBack: Boolean,
        cause: Throwable?,
    ) = Unit

    override fun onTotalFailure(primaryError: String, fallbackError: String) = Unit
}
