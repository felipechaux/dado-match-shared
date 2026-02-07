package com.dadomatch.shared.presentation.ui.screens

import androidx.compose.runtime.*
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions

@Composable
fun PaywallScreen(onDismiss: () -> Unit = {}) {
    Paywall(
        options = PaywallOptions(
            dismissRequest = onDismiss
        ) {
            shouldDisplayDismissButton = true
        }
    )
}
