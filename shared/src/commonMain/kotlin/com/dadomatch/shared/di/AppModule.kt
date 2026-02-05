package com.dadomatch.shared.di

import com.dadomatch.shared.presentation.viewmodel.HomeViewModel
import com.dadomatch.shared.BuildKonfig
import com.dadomatch.shared.data.remote.GeminiService
import com.dadomatch.shared.data.repository.IcebreakerRepositoryImpl
import com.dadomatch.shared.data.repository.SuccessRepositoryImpl
import com.dadomatch.shared.domain.repository.SuccessRepository
import com.dadomatch.shared.domain.usecase.*
import com.dadomatch.shared.presentation.viewmodel.SuccessesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // Data Source
    single { 
        GeminiService(
            apiKey = BuildKonfig.GEMINI_API_KEY,
            modelName = BuildKonfig.GEMINI_MODEL_NAME
        ) 
    }
    
    // Repository
    singleOf(::IcebreakerRepositoryImpl) bind IcebreakerRepository::class
    singleOf(::SuccessRepositoryImpl) bind SuccessRepository::class
    
    // UseCase
    factoryOf(::GenerateIcebreakerUseCase)
    factoryOf(::SubmitFeedbackUseCase)
    factoryOf(::AddSuccessUseCase)
    factoryOf(::GetSuccessesUseCase)
    
    // ViewModel
    viewModelOf(::HomeViewModel)
    viewModelOf(::SuccessesViewModel)
}
