package com.dadomatch.shared.feature.icebreaker.di

import com.dadomatch.shared.BuildKonfig
import com.dadomatch.shared.feature.icebreaker.data.remote.IcebreakerAiService
import com.dadomatch.shared.feature.icebreaker.data.remote.OpenAiCompatibleService
import com.dadomatch.shared.feature.icebreaker.data.remote.OpenAiCompatibleService.ThinkingOff
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

    // Dedicated Ktor client shared by both OpenAI-compatible providers. The router
    // caps each attempt anyway; these only bound a connection that never answers.
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

    single(named("nvidia")) {
        OpenAiCompatibleService(
            httpClient = get(named("aiHttpClient")),
            providerId = "nvidia",
            apiKey = BuildKonfig.NVIDIA_API_KEY,
            baseUrl = BuildKonfig.NVIDIA_BASE_URL,
            modelName = BuildKonfig.NVIDIA_MODEL_NAME,
            thinkingOff = ThinkingOff.CHAT_TEMPLATE_KWARGS,
        )
    }

    // Gemini through its OpenAI compatibility layer rather than the Gemini SDK, which
    // can neither turn thinking off (newer Flash models take 6-12s with it) nor reach
    // models that are only served on v1beta.
    single(named("gemini")) {
        OpenAiCompatibleService(
            httpClient = get(named("aiHttpClient")),
            providerId = "gemini",
            apiKey = BuildKonfig.GEMINI_API_KEY,
            baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai",
            modelName = BuildKonfig.GEMINI_MODEL_NAME,
            premiumModelName = BuildKonfig.GEMINI_PREMIUM_MODEL_NAME,
            thinkingOff = ThinkingOff.MINIMAL_REASONING_EFFORT,
        )
    }

    // AI observability: latency/winner → Analytics, failures/fallbacks → Crashlytics.
    // Safe to construct even before Firebase is initialised in the host app.
    single<AiTelemetry> { CrashlyticsAiTelemetry() }

    // Primary = NVIDIA, fallback = Gemini.
    single<IcebreakerAiService> {
        RoutingIcebreakerService(
            primary = get<OpenAiCompatibleService>(named("nvidia")),
            fallback = get<OpenAiCompatibleService>(named("gemini")),
            telemetry = get()
        )
    }

    singleOf(::IcebreakerRepositoryImpl) bind IcebreakerRepository::class
    factory { GenerateIcebreakerUseCase(get(), get<SubscriptionRepository>(), get<AiTelemetry>()) }
    factoryOf(::SubmitFeedbackUseCase)
    factoryOf(::RollDiceUseCase)
    viewModelOf(::HomeViewModel)
}
