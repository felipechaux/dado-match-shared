package com.dadomatch.shared.di

import com.dadomatch.shared.feature.auth.presentation.AndroidAuthHandler
import com.dadomatch.shared.feature.auth.presentation.NativeAuthHandler
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<NativeAuthHandler> { AndroidAuthHandler(get()) }
}
