package com.dadomatch.shared.feature.auth.presentation

import com.dadomatch.shared.feature.auth.presentation.IosAuthHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A Kotlin object accessible from Swift that retrieves the [IosAuthHandler]
 * singleton from Koin and exposes it so Swift can set the native sign-in providers.
 *
 * Usage from Swift (in AppDelegate.didFinishLaunchingWithOptions, after initKoin()):
 *
 *   let handler = IosAuthHandlerRegistry().getHandler()
 *   handler.googleSignInProvider = { callback in ... }
 *   handler.appleSignInProvider  = { callback in ... }
 */
class IosAuthHandlerRegistry : KoinComponent {
    private val handler: IosAuthHandler by inject()

    fun getHandler(): IosAuthHandler = handler
}
