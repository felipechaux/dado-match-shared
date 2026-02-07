package com.dadomatch.shared

import com.dadomatch.shared.di.getAllModules
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin {
            appDeclaration()
            modules(getAllModules())
        }
        
        // Initialize RevenueCat after Koin is started
        val koin = KoinPlatformTools.defaultContext().get()
        val revenueCatService = koin.get<com.dadomatch.shared.data.remote.RevenueCatService>()
        revenueCatService.configure(BuildKonfig.REVENUECAT_API_KEY)
    }
}

// Called from iOS
fun initKoin() = initKoin {}
