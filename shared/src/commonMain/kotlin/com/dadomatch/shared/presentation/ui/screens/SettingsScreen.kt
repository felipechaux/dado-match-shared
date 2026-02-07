package com.dadomatch.shared.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.domain.model.SubscriptionTier
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.presentation.viewmodel.SubscriptionViewModel
import com.dadomatch.shared.presentation.ui.extensions.getLocalizedPeriod
import com.dadomatch.shared.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPaywall: () -> Unit = {}
) {
    val viewModel: SubscriptionViewModel = koinViewModel()
    val subscriptionStatus by viewModel.subscriptionStatus.collectAsState()
    
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = DeepDarkBlue,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(Res.string.settings_title), 
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subscription Section
            SubscriptionCard(
                tier = subscriptionStatus?.tier ?: SubscriptionTier.FREE,
                rollsRemaining = subscriptionStatus?.dailyRollsRemaining,
                isLifetime = subscriptionStatus?.isLifetime ?: false,
                renewalPeriod = subscriptionStatus?.renewalPeriod,
                onUpgradeClick = onNavigateToPaywall
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // General Settings Section
            SettingsSectionTitle(stringResource(Res.string.settings_section_preferences))
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = stringResource(Res.string.settings_item_notifications),
                subtitle = stringResource(Res.string.settings_item_notifications_desc),
                onClick = {}
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsSectionTitle(stringResource(Res.string.settings_section_support))
            SettingsItem(
                icon = Icons.Default.Info,
                title = stringResource(Res.string.settings_item_help),
                subtitle = stringResource(Res.string.settings_item_help_desc),
                onClick = {}
            )
            
            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom bar
        }
    }
}

@Composable
fun SubscriptionCard(
    tier: SubscriptionTier,
    rollsRemaining: Int?,
    isLifetime: Boolean = false,
    renewalPeriod: String? = null,
    onUpgradeClick: () -> Unit
) {
    val isPro = tier == SubscriptionTier.PREMIUM
    
    Box(
        modifier = Modifier
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
                    text = stringResource(Res.string.subs_rolls_left, rollsRemaining),
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

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = NeonCyan,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(NeonCyan.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextWhite.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Chevron (optional)
    }
}
