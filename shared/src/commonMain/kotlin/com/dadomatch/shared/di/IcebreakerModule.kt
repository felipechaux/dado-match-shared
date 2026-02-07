package com.dadomatch.shared.di

import com.dadomatch.shared.BuildKonfig
import com.dadomatch.shared.data.remote.GeminiService
import com.dadomatch.shared.data.repository.IcebreakerRepositoryImpl
import com.dadomatch.shared.domain.repository.IcebreakerRepository
import com.dadomatch.shared.domain.usecase.GenerateIcebreakerUseCase
import com.dadomatch.shared.domain.usecase.SubmitFeedbackUseCase
import com.dadomatch.shared.presentation.viewmodel.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Feature: Icebreakers (AI Generation)
 */
val icebreakerModule = module {
    single { 
        GeminiService(
            apiKey = BuildKonfig.GEMINI_API_KEY,
            modelName = BuildKonfig.GEMINI_MODEL_NAME
        ) 
    }
    singleOf(::IcebreakerRepositoryImpl) bind IcebreakerRepository::class
    factoryOf(::GenerateIcebreakerUseCase)
    factoryOf(::SubmitFeedbackUseCase)
    viewModelOf(::HomeViewModel)
}
