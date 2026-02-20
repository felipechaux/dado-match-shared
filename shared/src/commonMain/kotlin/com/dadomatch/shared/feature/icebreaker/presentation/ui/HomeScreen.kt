package com.dadomatch.shared.feature.icebreaker.presentation.ui
 
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.ActionChoiceDialog
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.FeedbackDialog
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.IcebreakerDialog
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.LaunchButton
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.RizzDice
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.SelectorGroup
import com.dadomatch.shared.feature.icebreaker.presentation.viewmodel.HomeViewModel
import com.dadomatch.shared.feature.onboarding.presentation.ui.OnboardingScreen
import com.dadomatch.shared.presentation.ui.components.AppLogo
import com.dadomatch.shared.presentation.ui.components.EmptyState
import com.dadomatch.shared.presentation.ui.theme.AppConstants
import com.dadomatch.shared.presentation.ui.theme.AppTheme
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.TextGray
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.environment_label
import com.dadomatch.shared.shared.generated.resources.error_button_retry
import com.dadomatch.shared.shared.generated.resources.error_message_generic
import com.dadomatch.shared.shared.generated.resources.error_title
import com.dadomatch.shared.shared.generated.resources.intensity_label
import com.dadomatch.shared.shared.generated.resources.ok_button
import com.dadomatch.shared.shared.generated.resources.rolling_rizz
import com.dadomatch.shared.shared.generated.resources.slogan
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import com.dadomatch.shared.feature.auth.presentation.ui.AuthBottomSheet
import com.dadomatch.shared.feature.auth.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPaywall: () -> Unit = {}
) {
    var selectedEnvironment by remember { mutableStateOf(AppConstants.ENVIRONMENTS[1]) } // Party/Fiesta
    var selectedIntensity by remember { mutableStateOf(AppConstants.INTENSITIES[3]) } // Funny/Gracioso
    var rolling by remember { mutableStateOf(false) }

    val environments = AppConstants.ENVIRONMENTS
    val intensities = AppConstants.INTENSITIES
    
    val viewModel: HomeViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { targetValue ->
            // Prevent swipe-to-dismiss while sign-in is in progress
            if (authUiState.isLoading && targetValue == SheetValue.Hidden) false
            else true
        }
    )

    Box(modifier = Modifier.fillMaxSize().background(DeepDarkBlue)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Logo & Title
            Box(modifier = Modifier.fillMaxWidth()) {
                AppLogo(modifier = Modifier.align(Alignment.Center))
                
                val isAnonymous = authUiState.user?.isAnonymous ?: true
                if (isAnonymous) {
                    IconButton(
                        onClick = { viewModel.showAuth() },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = TextGray
                        )
                    }
                }
            }
        
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(Res.string.slogan),
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            // Rizz Component
            RizzDice(
                rolling = rolling,
                onRollComplete = { result ->
                    rolling = false
                    val currentLanguage = Locale.current.language
                    viewModel.onRollComplete(selectedEnvironment, selectedIntensity, currentLanguage)
                },
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Environment Selector
            SelectorGroup(
                title = stringResource(Res.string.environment_label),
                options = environments,
                selectedOption = selectedEnvironment,
                onOptionSelected = { selectedEnvironment = it },
                selectionColorProvider = { AppConstants.getEnvironmentColor(it) },
                iconProvider = { AppConstants.getEnvironmentIcon(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Intensity Selector
            SelectorGroup(
                title = stringResource(Res.string.intensity_label),
                options = intensities,
                selectedOption = selectedIntensity,
                onOptionSelected = { selectedIntensity = it },
                selectionColorProvider = { AppConstants.getIntensityColor(it) },
                iconProvider = { AppConstants.getIntensityIcon(it) },
                isRestricted = { option -> option == "int_spicy" && !uiState.isPremium }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Lanzar Button
            LaunchButton(
                onClick = {
                    rolling = true
                    viewModel.dismissIcebreaker()
                },
                environmentColor = AppConstants.getEnvironmentColor(selectedEnvironment),
                intensityColor = AppConstants.getIntensityColor(selectedIntensity)
            )
            
            // Spacer to account for the floating footer menu
            if (!uiState.isPremium) {
                Spacer(modifier = Modifier.height(100.dp))
            } else {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Icebreaker Modal/Overlay
        if (uiState.showIcebreaker) {
            IcebreakerDialog(
                text = uiState.currentIcebreaker,
                onDismiss = { viewModel.onIcebreakerDismissed() }
            )
        }

        if (uiState.showActionChoices) {
            ActionChoiceDialog(
                onChoice = { used -> viewModel.onActionChoice(used) }
            )
        }

        if (uiState.showOnboarding) {
            OnboardingScreen(
                onDismiss = { viewModel.completeOnboarding() }
            )
        }

        if (uiState.showFeedbackDialog) {
            FeedbackDialog(
                onSubmit = { feedback -> viewModel.onSubmitFeedback(feedback) }
            )
        }

        if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepDarkBlue.copy(alpha = 0.95f))
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EmptyState(
                        title = stringResource(Res.string.error_title),
                        description = stringResource(Res.string.error_message_generic),
                        icon = "😵‍💫",
                        buttonText = stringResource(Res.string.error_button_retry),
                        onButtonClick = {
                            viewModel.dismissError()
                            viewModel.onRetryRoll()
                        }
                    )
                    
                    TextButton(onClick = { viewModel.dismissError() }) {
                        Text(stringResource(Res.string.ok_button), color = TextGray)
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonCyan)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(Res.string.rolling_rizz), color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        if (uiState.showAuth) {
            ModalBottomSheet(
                // Block dismiss while sign-in is loading (e.g. Google web view is open)
                onDismissRequest = { if (!authUiState.isLoading) viewModel.hideAuth() },
                sheetState = sheetState,
                containerColor = Color.Transparent,
                scrimColor = Color.Black.copy(alpha = 0.5f)
            ) {
                AuthBottomSheet(
                    viewModel = authViewModel,
                    onDismiss = { viewModel.hideAuth() }
                )
            }
        }

        if (uiState.showPaywallNavigation) {
            LaunchedEffect(uiState.showPaywallNavigation) {
                onNavigateToPaywall()
                viewModel.onPaywallNavigated()
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeScreen()
    }
}
