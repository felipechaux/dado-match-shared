package com.dadomatch.shared.feature.icebreaker.di

import com.dadomatch.shared.BuildKonfig
import com.dadomatch.shared.feature.icebreaker.data.remote.GeminiService
import com.dadomatch.shared.feature.icebreaker.data.remote.IcebreakerAiService
import com.dadomatch.shared.feature.icebreaker.data.remote.NvidiaService
import com.dadomatch.shared.feature.icebreaker.data.remote.RoutingIcebreakerService
import com.dadomatch.shared.feature.icebreaker.data.repository.IcebreakerRepositoryImpl
import com.dadomatch.shared.feature.icebreaker.data.telemetry.AiTelemetry
import com.dadomatch.shared.feature.icebreaker.data.telemetry.CrashlyticsAiTelemetry
import com.dadomatch.shared.feature.icebreaker.domain.repository.IcebreakerRepository
import com.dadomatch.shared.feature.icebreaker.domain.usecase.GenerateIcebreakerUseCase
import com.dadomatch.shared.feature.icebreaker.domain.usecase.RollDiceUseCase
import com.dadomatch.shared.feature.icebreaker.domain.usecase.SubmitFeedbackUseCase
import com.dadomatch.shared.feature.icebreaker.presentation.viewmodel.HomeViewModel
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Feature: Icebreakers (AI Generation)
 *
 * Generation is routed NVIDIA-first (fast) with an automatic Gemini fallback.
 */
val icebreakerModule = module {

    // Dedicated Ktor client for the OpenAI-compatible NVIDIA endpoint.
    // Short timeouts mean a slow/unreachable provider fails fast and the router
    // falls back to Gemini instead of leaving the user waiting.
    single(named("aiHttpClient")) {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 5_000
                requestTimeoutMillis = 12_000
                socketTimeoutMillis = 12_000
            }
        }
    }

    single {
        GeminiService(
            apiKey = BuildKonfig.GEMINI_API_KEY,
            modelName = BuildKonfig.GEMINI_MODEL_NAME,
            premiumModelName = BuildKonfig.GEMINI_PREMIUM_MODEL_NAME
        )
    }

    single {
        NvidiaService(
            httpClient = get(named("aiHttpClient")),
            apiKey = BuildKonfig.NVIDIA_API_KEY,
            modelName = BuildKonfig.NVIDIA_MODEL_NAME,
            baseUrl = BuildKonfig.NVIDIA_BASE_URL
        )
    }

    // AI observability: latency/winner → Analytics, failures/fallbacks → Crashlytics.
    // Safe to construct even before Firebase is initialised in the host app.
    single<AiTelemetry> { CrashlyticsAiTelemetry() }

    // Primary = NVIDIA, fallback = Gemini.
    single<IcebreakerAiService> {
        RoutingIcebreakerService(
            primary = get<NvidiaService>(),
            fallback = get<GeminiService>(),
            telemetry = get()
        )
    }

    singleOf(::IcebreakerRepositoryImpl) bind IcebreakerRepository::class
    factory { GenerateIcebreakerUseCase(get(), get<SubscriptionRepository>(), get<AiTelemetry>()) }
    factoryOf(::SubmitFeedbackUseCase)
    factoryOf(::RollDiceUseCase)
    viewModelOf(::HomeViewModel)
}
