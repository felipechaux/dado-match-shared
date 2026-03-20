package com.dadomatch.shared.feature.auth.di

import com.dadomatch.shared.feature.auth.data.repository.AuthRepositoryImpl
import com.dadomatch.shared.feature.auth.domain.repository.AuthRepository
import com.dadomatch.shared.feature.auth.domain.usecase.SignInAnonymouslyUseCase
import com.dadomatch.shared.feature.auth.domain.usecase.SignInWithAppleUseCase
import com.dadomatch.shared.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.dadomatch.shared.feature.auth.presentation.viewmodel.AuthViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class

    factoryOf(::SignInWithGoogleUseCase)
    factoryOf(::SignInWithAppleUseCase)
    factoryOf(::SignInAnonymouslyUseCase)

    viewModelOf(::AuthViewModel)
}
