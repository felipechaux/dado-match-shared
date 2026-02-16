package com.dadomatch.shared.di

import com.dadomatch.shared.feature.auth.presentation.IosAuthHandler
import com.dadomatch.shared.feature.auth.presentation.NativeAuthHandler
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<NativeAuthHandler> { IosAuthHandler() }
}
