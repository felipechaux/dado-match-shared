package com.dadomatch.shared.feature.subscription.presentation.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.presentation.viewmodel.SubscriptionViewModel
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.subs_rolls_left
import com.dadomatch.shared.shared.generated.resources.subs_login_required_title
import com.dadomatch.shared.shared.generated.resources.subs_login_required_desc
import com.dadomatch.shared.shared.generated.resources.subs_login_button
import com.dadomatch.shared.shared.generated.resources.subs_maybe_later
import com.dadomatch.shared.feature.auth.presentation.viewmodel.AuthViewModel
import com.dadomatch.shared.feature.auth.presentation.ui.AuthBottomSheet
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.presentation.ui.theme.TextGray
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(onDismiss: () -> Unit = {}) {
    val viewModel: SubscriptionViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    
    val status by viewModel.subscriptionStatus.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    
    val isAnonymous = authUiState.user?.isAnonymous ?: true
    var showAuthSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { targetValue ->
            if (authUiState.isLoading && targetValue == SheetValue.Hidden) false
            else true
        }
    )
    
    Box(modifier = Modifier.fillMaxSize().background(DeepDarkBlue)) {
        if (isAnonymous) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.subs_login_required_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.subs_login_required_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { showAuthSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text(stringResource(Res.string.subs_login_button), color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.subs_maybe_later), color = TextGray)
                }
            }
            
            if (showAuthSheet) {
                ModalBottomSheet(
                    onDismissRequest = { 
                        if (!authUiState.isLoading) showAuthSheet = false 
                    },
                    sheetState = sheetState,
                    containerColor = Color.Transparent,
                    scrimColor = Color.Black.copy(alpha = 0.5f)
                ) {
                    AuthBottomSheet(
                        viewModel = authViewModel,
                        onDismiss = { showAuthSheet = false }
                    )
                }
            }
        } else {
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
}
