package com.dadomatch.shared.feature.icebreaker.data.remote

import com.dadomatch.shared.core.util.Resource
import com.dadomatch.shared.feature.icebreaker.data.telemetry.AiTelemetry
import com.dadomatch.shared.feature.icebreaker.data.telemetry.NoOpAiTelemetry
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException

/**
 * Tries [primary] first (fast path); on any failure transparently retries with
 * [fallback]. This is what gives us NVIDIA's speed while keeping Gemini's
 * resilience — if NVIDIA errors, rate-limits, or times out, Gemini answers instead.
 *
 * This is also the single place AI telemetry is emitted: per-attempt latency/winner
 * for performance analysis, plus non-fatals for failures and the total-failure case.
 * Keeping it here means the leaf services ([NvidiaService], [GeminiService]) stay pure
 * and free of any Firebase dependency.
 */
class RoutingIcebreakerService(
    private val primary: IcebreakerAiService,
    private val fallback: IcebreakerAiService,
    private val telemetry: AiTelemetry = NoOpAiTelemetry,
) : IcebreakerAiService {

    override val providerId: String = "routing"

    override suspend fun generateIcebreaker(
        environment: String,
        intensity: String,
        language: String,
        usePremiumModel: Boolean,
    ): Resource<String> {
        // Top-level safety net: if either leaf service throws something its own try/catch
        // missed (e.g. a serialization crash, Ktor null, OOM, Firebase init failure), make
        // sure it lands in Crashlytics before we rethrow / surface a generic error.
        return try {
            route(environment, intensity, language, usePremiumModel)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            telemetry.onUnexpectedError(stage = "routing", cause = e)
            Resource.Error(e.message ?: "ai_unexpected_error", e)
        }
    }

    private suspend fun route(
        environment: String,
        intensity: String,
        language: String,
        usePremiumModel: Boolean,
    ): Resource<String> {
        val primaryResult = safeAttempt(primary, environment, intensity, language, usePremiumModel)
        telemetry.onProviderAttempt(
            provider = primary.providerId,
            success = primaryResult.value is Resource.Success,
            latencyMs = primaryResult.millis,
            premium = usePremiumModel,
            fellBack = false,
            errorCode = (primaryResult.value as? Resource.Error)?.message,
        )
        if (primaryResult.value is Resource.Success) return primaryResult.value

        // Primary failed → record it, then fall back.
        val primaryError = primaryResult.value as Resource.Error
        telemetry.onProviderFailure(primary.providerId, primaryError.message, fellBack = false, cause = primaryError.throwable)

        val fallbackResult = safeAttempt(fallback, environment, intensity, language, usePremiumModel)
        telemetry.onProviderAttempt(
            provider = fallback.providerId,
            success = fallbackResult.value is Resource.Success,
            latencyMs = fallbackResult.millis,
            premium = usePremiumModel,
            fellBack = true,
            errorCode = (fallbackResult.value as? Resource.Error)?.message,
        )

        // If the fallback also fails, surface its error — and flag the total failure.
        (fallbackResult.value as? Resource.Error)?.let { fallbackError ->
            telemetry.onProviderFailure(fallback.providerId, fallbackError.message, fellBack = true, cause = fallbackError.throwable)
            telemetry.onTotalFailure(primaryError.message, fallbackError.message)
        }
        return fallbackResult.value
    }

    /**
     * Run a single provider attempt, timed. Cancellation propagates; everything else
     * is funneled into telemetry as an unexpected error and turned into a Resource.Error
     * so the router can still try the fallback.
     */
    private suspend fun safeAttempt(
        service: IcebreakerAiService,
        environment: String,
        intensity: String,
        language: String,
        usePremiumModel: Boolean,
    ): Timed<Resource<String>> {
        val start = TimeSource.Monotonic.markNow()
        val value: Resource<String> = try {
            service.generateIcebreaker(environment, intensity, language, usePremiumModel)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            telemetry.onUnexpectedError(stage = "provider:${service.providerId}", cause = e)
            Resource.Error(e.message ?: "ai_unexpected_error", e)
        }
        return Timed(value, start.elapsedNow().inWholeMilliseconds)
    }

    private data class Timed<T>(val value: T, val millis: Long)
}
