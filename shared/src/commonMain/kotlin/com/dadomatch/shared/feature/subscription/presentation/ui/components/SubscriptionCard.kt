package com.dadomatch.shared.feature.subscription.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionStatus
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.presentation.viewmodel.SubscriptionViewModel
import com.dadomatch.shared.presentation.ui.extensions.getLocalizedPeriod
import com.dadomatch.shared.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * A reusable component that displays the user's current subscription status.
 * This can be used in the Settings screen or any other screen that needs to show plan info.
 */
@Composable
fun SubscriptionCard(
    modifier: Modifier = Modifier,
    status: SubscriptionStatus?,
    onUpgradeClick: () -> Unit
) {
    val tier = status?.tier ?: SubscriptionTier.FREE
    val rollsRemaining = status?.dailyRollsRemaining
    val isLifetime = status?.isLifetime ?: false
    val renewalPeriod = status?.renewalPeriod
    
    val isPro = tier == SubscriptionTier.PREMIUM
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = if (isPro) {
                        listOf(NeonPink.copy(alpha = 0.8f), Color(0xFF8E24AA))
                    } else {
                        listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.08f))
                    }
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.subs_current_plan),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isPro) TextWhite.copy(alpha = 0.8f) else TextWhite.copy(alpha = 0.6f)
                    )
                    val tierText = if (isLifetime) {
                        stringResource(Res.string.subs_tier_lifetime)
                    } else if (isPro) {
                        val period = renewalPeriod?.getLocalizedPeriod()
                        if (period != null) {
                            "${stringResource(Res.string.subs_tier_pro)} ($period)"
                        } else {
                            stringResource(Res.string.subs_tier_pro)
                        }
                    } else {
                        stringResource(Res.string.subs_tier_free)
                    }
                    
                    Text(
                        text = tierText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isPro) TextWhite else NeonCyan,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (isPro) Color(0xFFFFD700) else TextWhite.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isPro) {
                    stringResource(Res.string.subs_pro_desc)
                } else {
                    stringResource(Res.string.subs_free_desc)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextWhite.copy(alpha = 0.8f)
            )
            
            if (!isPro && rollsRemaining != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.subs_rolls_left).replace("%d", (rollsRemaining).toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonPink,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPro) Color.White.copy(alpha = 0.2f) else NeonCyan
                )
            ) {
                Text(
                    text = if (isPro) {
                        stringResource(Res.string.subs_manage_button)
                    } else {
                        stringResource(Res.string.subs_upgrade_button)
                    },
                    color = if (isPro) TextWhite else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * A version of SubscriptionCard that observes state from SubscriptionViewModel.
 */
@Composable
fun SubscriptionCard(
    modifier: Modifier = Modifier,
    viewModel: SubscriptionViewModel,
    @Suppress("UNUSED_PARAMETER") isAnonymous: Boolean = false,
    onNavigateToPaywall: () -> Unit
) {
    val subscriptionStatus by viewModel.subscriptionStatus.collectAsState()

    // The repository already handles anonymous/pro logic correctly.
    // We simply display whatever status the ViewModel provides — no local override.
    SubscriptionCard(
        modifier = modifier,
        status = subscriptionStatus,
        onUpgradeClick = onNavigateToPaywall
    )
}
