package com.dadomatch.shared.feature.subscription.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.viewmodel.SubscriptionViewModel
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.subs_rolls_left
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The paywall is reachable — and purchasable — without an account.
 *
 * App Store guideline 5.1.1(v) forbids requiring registration to buy In-App Purchases
 * that are not account based, so this screen renders RevenueCat's paywall for every
 * user. A guest purchase binds to the Apple ID receipt; signing in later (offered as
 * an option in Home and Settings) only adds cross-device convenience.
 */
@Composable
fun PaywallScreen(onDismiss: () -> Unit = {}, onPurchaseSuccess: () -> Unit = {}) {
    val viewModel: SubscriptionViewModel = koinViewModel()

    val uiState by viewModel.uiState.collectAsState()
    val status = uiState.subscriptionStatus

    // The RevenueCat Paywall's PaywallListener does not reliably fire on iOS, so the
    // purchase is detected from the shared subscription status instead: a FREE→PREMIUM
    // transition while this screen is visible means the user just purchased.
    var wasFree by remember { mutableStateOf(false) }
    LaunchedEffect(status?.tier) {
        when {
            status == null -> Unit
            status.tier == SubscriptionTier.FREE -> wasFree = true
            status.tier == SubscriptionTier.PREMIUM && wasFree -> {
                wasFree = false
                onPurchaseSuccess()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepDarkBlue)) {
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
                        onPurchaseSuccess()
                    }

                    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                        onPurchaseSuccess()
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
