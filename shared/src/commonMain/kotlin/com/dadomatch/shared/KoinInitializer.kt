package com.dadomatch.shared

import com.dadomatch.shared.di.appModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin {
            appDeclaration()
            modules(appModule)
        }
    }
}

// Called from iOS
fun initKoin() = initKoin {}
