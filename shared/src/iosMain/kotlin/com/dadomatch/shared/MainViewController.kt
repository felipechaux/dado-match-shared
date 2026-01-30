
package com.dadomatch.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.dadomatch.shared.presentation.ui.navigation.AppNavigation

fun MainViewController() = ComposeUIViewController(
    configure = {
        enforceStrictPlistSanityCheck = false
    }
) {
    AppNavigation()
}

