package com.dadomatch.shared.feature.subscription.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dadomatch.shared.feature.auth.presentation.viewmodel.AuthViewModel
import com.dadomatch.shared.feature.subscription.presentation.ui.components.SubscriptionCard
import com.dadomatch.shared.presentation.ui.components.ConfettiOverlay
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.presentation.viewmodel.SubscriptionViewModel
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPaywall: () -> Unit = {}
) {
    val viewModel: SubscriptionViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val scrollState = rememberScrollState()
    val showConfetti by viewModel.showConfetti.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val isAnonymous = authUiState.user?.isAnonymous ?: true

    Box(modifier = Modifier.fillMaxSize()) {
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
                SubscriptionCard(
                    viewModel = viewModel,
                    isAnonymous = isAnonymous,
                    onNavigateToPaywall = onNavigateToPaywall
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // General Settings Section omitted for launch
                // TODO: Re-add Settings and Support Sections once functionality is ready
                
                Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom bar
            }
        }
        
        if (showConfetti) {
            ConfettiOverlay(
                onAnimationEnd = {
                    viewModel.onConfettiConsumed()
                }
            )
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
        
    }
}
