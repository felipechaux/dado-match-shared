package com.dadomatch.shared.feature.icebreaker.data.remote

import com.dadomatch.shared.core.util.Resource
import com.dadomatch.shared.feature.icebreaker.data.telemetry.AiTelemetry
import com.dadomatch.shared.feature.icebreaker.data.telemetry.NoOpAiTelemetry
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Tries [primary] first (fast path). If it fails, or has not answered within
 * [hedgeAfterMs], [fallback] is started in parallel and the first success wins —
 * a slow or overloaded provider costs at most the head start, not its full timeout.
 * Every attempt is capped at [attemptTimeoutMs].
 *
 * This is also the single place AI telemetry is emitted: per-attempt latency/winner
 * for performance analysis, plus non-fatals for failures and the total-failure case.
 * Keeping it here means the leaf services ([OpenAiCompatibleService]) stay pure
 * and free of any Firebase dependency.
 */
class RoutingIcebreakerService(
    private val primary: IcebreakerAiService,
    private val fallback: IcebreakerAiService,
    private val telemetry: AiTelemetry = NoOpAiTelemetry,
    private val hedgeAfterMs: Long = 2_500,
    private val attemptTimeoutMs: Long = 10_000,
) : IcebreakerAiService {

    override val providerId: String = "routing"

    override suspend fun generateIcebreaker(
        environment: String,
        intensity: String,
        language: String,
        usePremiumModel: Boolean,
    ): Resource<String> = routeSafely(usePremiumModel) {
        it.generateIcebreaker(environment, intensity, language, usePremiumModel)
    }

    override suspend fun complete(prompt: String, maxTokens: Int, usePremiumModel: Boolean): Resource<String> =
        routeSafely(usePremiumModel) { it.complete(prompt, maxTokens, usePremiumModel) }

    private suspend fun routeSafely(
        usePremiumModel: Boolean,
        call: suspend (IcebreakerAiService) -> Resource<String>,
    ): Resource<String> {
        // Top-level safety net: if either leaf service throws something its own try/catch
        // missed (e.g. a serialization crash, Ktor null, OOM, Firebase init failure), make
        // sure it lands in Crashlytics before we rethrow / surface a generic error.
        return try {
            route(usePremiumModel, call)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            telemetry.onUnexpectedError(stage = "routing", cause = e)
            Resource.Error(e.message ?: "ai_unexpected_error", e)
        }
    }

    private suspend fun route(
        usePremiumModel: Boolean,
        call: suspend (IcebreakerAiService) -> Resource<String>,
    ): Resource<String> = coroutineScope {
        val primaryAttempt = async { attempt(primary, usePremiumModel, fellBack = false, call) }

        // Head start: most of the time the primary answers within it and the
        // fallback is never called.
        val early = withTimeoutOrNull(hedgeAfterMs) { primaryAttempt.await() }
        if (early is Resource.Success) return@coroutineScope early

        // Primary failed or is slow → race the fallback against it instead of waiting
        // the primary out; the first success wins and the loser is cancelled.
        val fallbackAttempt = async { attempt(fallback, usePremiumModel, fellBack = true, call) }
        val first = select {
            primaryAttempt.onAwait { it to fallbackAttempt }
            fallbackAttempt.onAwait { it to primaryAttempt }
        }
        val (firstResult, other) = first
        if (firstResult is Resource.Success) {
            other.cancel()
            return@coroutineScope firstResult
        }
        val otherResult = other.await()
        if (otherResult is Resource.Success) return@coroutineScope otherResult

        // Both failed — surface the fallback's error and flag the total failure.
        val primaryError = primaryAttempt.await() as Resource.Error
        val fallbackError = fallbackAttempt.await() as Resource.Error
        telemetry.onTotalFailure(primaryError.message, fallbackError.message)
        fallbackError
    }

    /** One provider attempt with its telemetry: latency/winner, plus a non-fatal on failure. */
    private suspend fun attempt(
        service: IcebreakerAiService,
        usePremiumModel: Boolean,
        fellBack: Boolean,
        call: suspend (IcebreakerAiService) -> Resource<String>,
    ): Resource<String> {
        val result = safeAttempt(service, call)
        telemetry.onProviderAttempt(
            provider = service.providerId,
            success = result.value is Resource.Success,
            latencyMs = result.millis,
            premium = usePremiumModel,
            fellBack = fellBack,
            errorCode = (result.value as? Resource.Error)?.message,
        )
        (result.value as? Resource.Error)?.let { error ->
            telemetry.onProviderFailure(service.providerId, error.message, fellBack = fellBack, cause = error.throwable)
        }
        return result.value
    }

    /**
     * Run a single provider attempt, timed. Cancellation propagates; everything else
     * is funneled into telemetry as an unexpected error and turned into a Resource.Error
     * so the router can still try the fallback.
     */
    private suspend fun safeAttempt(
        service: IcebreakerAiService,
        call: suspend (IcebreakerAiService) -> Resource<String>,
    ): Timed<Resource<String>> {
        val start = TimeSource.Monotonic.markNow()
        val value: Resource<String> = try {
            // A provider that hangs (Gemini's SDK has no timeout by default) must not
            // keep the user waiting: past the cap it counts as "AI busy, retry".
            withTimeoutOrNull(attemptTimeoutMs) { call(service) }
                ?: Resource.Error("rate_limit_exceeded", AiTimeoutException(service.providerId, attemptTimeoutMs))
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

/** Synthetic throwable recording that a provider exceeded the attempt cap, for Crashlytics. */
class AiTimeoutException(provider: String, timeoutMs: Long) :
    Exception("$provider did not answer within ${timeoutMs}ms")
