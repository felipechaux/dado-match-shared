package com.dadomatch.shared.feature.success.di

import com.dadomatch.shared.core.data.AppDatabase
import com.dadomatch.shared.feature.success.data.repository.SuccessRepositoryImpl
import com.dadomatch.shared.feature.success.domain.repository.SuccessRepository
import com.dadomatch.shared.feature.success.domain.usecase.AddSuccessUseCase
import com.dadomatch.shared.feature.success.domain.usecase.GetSuccessesUseCase
import com.dadomatch.shared.presentation.viewmodel.SuccessesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Feature: Success Records
 */
val successModule = module {
    single { get<AppDatabase>().successDao() }
    singleOf(::SuccessRepositoryImpl) bind SuccessRepository::class
    factoryOf(::AddSuccessUseCase)
    factoryOf(::GetSuccessesUseCase)
    factory { 
        SuccessesViewModel(
            getSuccessesUseCase = get(),
            checkEntitlementUseCase = get()
        ) 
    }
}
