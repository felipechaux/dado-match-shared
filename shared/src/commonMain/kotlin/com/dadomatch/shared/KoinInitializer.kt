package com.dadomatch.shared

import com.dadomatch.shared.di.appModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule)
    }

// Called from iOS
fun initKoin() = initKoin {}
