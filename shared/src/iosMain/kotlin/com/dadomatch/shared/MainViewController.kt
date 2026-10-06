package com.dadomatch.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.dadomatch.shared.presentation.ui.navigation.AppNavigation
import com.dadomatch.shared.presentation.ui.navigation.NativeTabBarBridge

fun MainViewController() = ComposeUIViewController(
    configure = {
        enforceStrictPlistSanityCheck = false
    }
) {
    AppNavigation()
}

/**
 * Compose app without its own bottom bar: the Swift side renders a native
 * UITabBar (Liquid Glass on iOS 26) driven through [nativeTabBar].
 */
fun MainViewController(nativeTabBar: NativeTabBarBridge) = ComposeUIViewController(
    configure = {
        enforceStrictPlistSanityCheck = false
    }
) {
    AppNavigation(nativeTabBar = nativeTabBar)
}
