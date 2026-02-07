package com.dadomatch.shared.presentation.ui.screens

import androidx.compose.runtime.*
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import com.dadomatch.shared.presentation.viewmodel.SubscriptionViewModel
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.StoreTransaction
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.domain.model.SubscriptionTier
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.subs_rolls_left
import org.jetbrains.compose.resources.stringResource

@Composable
fun PaywallScreen(onDismiss: () -> Unit = {}) {
    val viewModel: SubscriptionViewModel = koinViewModel()
    val status by viewModel.subscriptionStatus.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
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

        // Rizz Counter Overlay for Free Users
        status?.let { s ->
            if (s.tier == SubscriptionTier.FREE) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 12.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = stringResource(Res.string.subs_rolls_left).replace("%d", (s.dailyRollsRemaining ?: 0).toString()),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
