package com.dadomatch.shared.di

import com.dadomatch.shared.feature.auth.presentation.IosAuthHandler
import com.dadomatch.shared.feature.auth.presentation.NativeAuthHandler
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // Register as the concrete type AND bind as the NativeAuthHandler interface.
    // The concrete type is needed by IosAuthHandlerRegistry which injects IosAuthHandler directly.
    single { IosAuthHandler() } bind NativeAuthHandler::class
}
