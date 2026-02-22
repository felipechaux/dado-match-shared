package com.dadomatch.shared

import com.dadomatch.shared.di.getAllModules
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        val revenueCatService = koin.get<com.dadomatch.shared.feature.subscription.data.remote.RevenueCatService>()
        revenueCatService.configure(BuildKonfig.REVENUECAT_API_KEY)

        // If user is already logged in (non-anonymous), sync their ID with RevenueCat
        // so that their Pro subscription is correctly restored on startup.
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val currentUser = Firebase.auth.currentUser
                if (currentUser != null && !currentUser.isAnonymous) {
                    revenueCatService.logIn(currentUser.uid)
                }
            } catch (e: Exception) {
                // Firebase may not be ready yet on some platforms — safe to ignore
            }
        }
    }
}

// Called from iOS
fun initKoin() = initKoin {}
