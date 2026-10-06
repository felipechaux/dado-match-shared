package com.dadomatch.shared.di

import com.dadomatch.shared.feature.auth.presentation.AndroidAuthHandler
import com.dadomatch.shared.feature.auth.presentation.CurrentActivityTracker
import com.dadomatch.shared.feature.auth.presentation.NativeAuthHandler
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // Eager: it must be registered before the first activity resumes
    single(createdAtStart = true) { CurrentActivityTracker(androidApplication()) }
    single<NativeAuthHandler> { AndroidAuthHandler(get(), get()) }
}
