package com.dadomatch.shared.presentation.ui.screens

import androidx.compose.runtime.*
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import com.dadomatch.shared.presentation.viewmodel.SubscriptionViewModel
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.StoreTransaction
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PaywallScreen(onDismiss: () -> Unit = {}) {
    val viewModel: SubscriptionViewModel = koinViewModel()
    
    Paywall(
        options = PaywallOptions(
            dismissRequest = onDismiss
        ) {
            shouldDisplayDismissButton = true
            listener = object : PaywallListener {
                override fun onPurchaseCompleted(
                    customerInfo: CustomerInfo,
                    storeTransaction: StoreTransaction
                ) {
                    // Just refresh status; the ViewModel will detect the tier change 
                    // and trigger the confetti celebration automatically.
                    viewModel.refreshStatus()
                }
                
                override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                    viewModel.refreshStatus()
                }
            }
        }
    )
}
