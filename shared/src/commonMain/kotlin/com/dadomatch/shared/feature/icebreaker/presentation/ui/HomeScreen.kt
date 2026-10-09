package com.dadomatch.shared.feature.icebreaker.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.feature.auth.presentation.ui.AuthBottomSheet
import com.dadomatch.shared.feature.onboarding.presentation.ui.OnboardingScreen
import com.dadomatch.shared.feature.auth.presentation.viewmodel.AuthViewModel
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.ActionChoiceDialog
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.FeedbackDialog
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.IcebreakerDialog
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.LaunchButton
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.LoadingOverlay
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.RizzDice
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.SelectorGroup
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.SignInNudgeBanner
import com.dadomatch.shared.feature.icebreaker.presentation.viewmodel.HomeViewModel
import com.dadomatch.shared.presentation.ui.components.AppLogo
import com.dadomatch.shared.presentation.ui.components.bottomBarClearance
import com.dadomatch.shared.presentation.ui.theme.AppConstants
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.StreakBadge
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.environment_label
import com.dadomatch.shared.shared.generated.resources.error_title
import com.dadomatch.shared.shared.generated.resources.game_entry
import com.dadomatch.shared.shared.generated.resources.get_unlimited_rolls
import com.dadomatch.shared.shared.generated.resources.intensity_label
import com.dadomatch.shared.shared.generated.resources.no_rolls_left
import com.dadomatch.shared.shared.generated.resources.ok_button
import com.dadomatch.shared.shared.generated.resources.no_internet_message
import com.dadomatch.shared.shared.generated.resources.no_internet_title
import com.dadomatch.shared.shared.generated.resources.rate_limit_message_free
import com.dadomatch.shared.shared.generated.resources.rate_limit_message_premium
import com.dadomatch.shared.shared.generated.resources.rate_limit_retry
import com.dadomatch.shared.shared.generated.resources.rate_limit_title
import com.dadomatch.shared.shared.generated.resources.rate_limit_upgrade
import com.dadomatch.shared.shared.generated.resources.rolling_rizz
import com.dadomatch.shared.shared.generated.resources.rolling_rizz_2
import com.dadomatch.shared.shared.generated.resources.rolling_rizz_3
import com.dadomatch.shared.shared.generated.resources.rolling_rizz_4
import com.dadomatch.shared.shared.generated.resources.rolling_rizz_5
import com.dadomatch.shared.shared.generated.resources.rolling_rizz_6
import com.dadomatch.shared.shared.generated.resources.surprise_me_button
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPaywall: () -> Unit = {},
    onNavigateToGame: () -> Unit = {}
) {
    val environments = AppConstants.ENVIRONMENTS
    val intensities  = AppConstants.INTENSITIES

    var selectedEnvironment by remember { mutableStateOf(environments[1]) }
    var selectedIntensity   by remember { mutableStateOf(intensities[3]) }
    var rolling             by remember { mutableStateOf(false) }

    val viewModel:     HomeViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val uiState     by viewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    // Only show banner after auth is initialized to avoid flashing on load
    val isAnonymous = authUiState.isInitialized && authUiState.user?.isAnonymous != false

    val deviceLanguage = Locale.current.language.take(2)
    LaunchedEffect(Unit) { viewModel.loadLanguage(deviceLanguage) }

    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { value ->
            !(authUiState.isLoading && value == SheetValue.Hidden)
        }
    )

    // Rotate loading message each time a load begins
    val loadingMessages = listOf(
        stringResource(Res.string.rolling_rizz),
        stringResource(Res.string.rolling_rizz_2),
        stringResource(Res.string.rolling_rizz_3),
        stringResource(Res.string.rolling_rizz_4),
        stringResource(Res.string.rolling_rizz_5),
        stringResource(Res.string.rolling_rizz_6),
    )
    var loadingMessage by remember { mutableStateOf(loadingMessages[0]) }
    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) loadingMessage = loadingMessages.random()
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepDarkBlue)) {

        // ── Main content — hidden until onboarding status is resolved ─────────
        if (uiState.isOnboardingReady && !uiState.showOnboarding) HomeContent(
            environments       = environments,
            intensities        = intensities,
            selectedEnvironment = selectedEnvironment,
            selectedIntensity   = selectedIntensity,
            rolling             = rolling,
            isAnonymous         = isAnonymous,
            isPremium           = uiState.isPremium,
            isLoading           = uiState.isLoading,
            streakDays          = uiState.streakDays,
            onEnvironmentSelected = { selectedEnvironment = it },
            onIntensitySelected   = { selectedIntensity   = it },
            onLaunch = {
                rolling = true
                viewModel.dismissIcebreaker()
            },
            onSurpriseMe = {
                val eligible = if (uiState.isPremium) intensities
                               else intensities.filter { it != "int_spicy" }
                selectedEnvironment = environments.random()
                selectedIntensity   = eligible.random()
                rolling = true
                viewModel.dismissIcebreaker()
            },
            onRollComplete = { _ ->
                rolling = false
                viewModel.onRollComplete(selectedEnvironment, selectedIntensity, uiState.selectedLanguage)
            },
            onSignInClick = { viewModel.showAuth() },
            onGameMode = onNavigateToGame
        )

        // ── Overlay layer ─────────────────────────────────────────────────────

        if (uiState.showAuthSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissAuth() },
                sheetState       = sheetState,
                containerColor   = DeepDarkBlue,
                dragHandle       = null
            ) {
                AuthBottomSheet(onDismiss = { viewModel.dismissAuth() })
            }
        }

        uiState.icebreaker?.let { text ->
            IcebreakerDialog(
                icebreakerText = text,
                onDismiss      = { viewModel.dismissIcebreaker() },
                onFeedback     = { viewModel.showFeedbackDialog() },
                onReroll       = {
                    rolling = true
                    viewModel.dismissIcebreaker()
                }
            )
        }

        uiState.actionChoiceIcebreaker?.let { text ->
            ActionChoiceDialog(
                icebreakerText = text,
                onDismiss      = { viewModel.dismissActionChoice() },
                onCopy         = { viewModel.copyToClipboard(text) },
                onShare        = { viewModel.shareIcebreaker(text) }
            )
        }

        if (uiState.showFeedbackDialog) {
            FeedbackDialog(
                onDismiss = { viewModel.dismissFeedbackDialog() },
                onSubmit  = { rating, comment ->
                    viewModel.submitFeedback(rating, comment)
                    viewModel.dismissFeedbackDialog()
                }
            )
        }

        if (uiState.showPaywallNudge) {
            IcebreakerDialog(
                icebreakerText = stringResource(Res.string.no_rolls_left),
                onDismiss      = { viewModel.dismissPaywallNudge() },
                customButton   = {
                    OutlinedButton(
                        onClick = {
                            viewModel.dismissPaywallNudge()
                            onNavigateToPaywall()
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(24.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        border   = BorderStroke(1.dp, NeonCyan)
                    ) {
                        Text(stringResource(Res.string.get_unlimited_rolls), color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        uiState.error?.let { errorMsg ->
            IcebreakerDialog(
                icebreakerText = errorMsg,
                title          = stringResource(Res.string.error_title),
                onDismiss      = { viewModel.clearError() },
                customButton   = {
                    TextButton(
                        onClick  = { viewModel.clearError() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.ok_button), color = NeonCyan)
                    }
                }
            )
        }

        if (uiState.noInternetError) {
            IcebreakerDialog(
                icebreakerText = stringResource(Res.string.no_internet_message),
                title          = stringResource(Res.string.no_internet_title),
                onDismiss      = { viewModel.clearNoInternetError() },
                customButton   = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick  = {
                                viewModel.clearNoInternetError()
                                viewModel.onRetryRoll()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape    = RoundedCornerShape(24.dp),
                            border   = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                        ) {
                            Text(stringResource(Res.string.rate_limit_retry), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        }

        if (uiState.rateLimitError) {
            val rateLimitMsg = if (uiState.isPremium)
                stringResource(Res.string.rate_limit_message_premium)
            else
                stringResource(Res.string.rate_limit_message_free)

            IcebreakerDialog(
                icebreakerText = rateLimitMsg,
                title          = stringResource(Res.string.rate_limit_title),
                onDismiss      = { viewModel.clearRateLimitError() },
                customButton   = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick  = {
                                viewModel.clearRateLimitError()
                                viewModel.onRetryRoll()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape    = RoundedCornerShape(24.dp),
                            border   = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                        ) {
                            Text(stringResource(Res.string.rate_limit_retry), fontWeight = FontWeight.SemiBold)
                        }
                        if (!uiState.isPremium) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick  = {
                                    viewModel.clearRateLimitError()
                                    onNavigateToPaywall()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(Res.string.rate_limit_upgrade), color = NeonCyan)
                            }
                        }
                    }
                }
            )
        }

        if (uiState.isLoading && !rolling) {
            LoadingOverlay(message = loadingMessage)
        }

        // ── Onboarding overlay — shown once on first launch ───────────────────
        if (uiState.showOnboarding) {
            OnboardingScreen(onDismiss = { viewModel.completeOnboarding() })
        }
    }
}

// ── Main content composable ───────────────────────────────────────────────────

// Below this height (most phones) the header goes inline and spacing tightens
// so everything, Surprise me included, fits above the bottom bar without scrolling
private val CompactHeight = 840.dp
// Keeps the controls a comfortable width on tablets and landscape
private val MaxContentWidth = 480.dp

@Composable
private fun HomeContent(
    environments: List<String>,
    intensities: List<String>,
    selectedEnvironment: String,
    selectedIntensity: String,
    rolling: Boolean,
    isAnonymous: Boolean,
    isPremium: Boolean,
    isLoading: Boolean,
    streakDays: Int,
    onEnvironmentSelected: (String) -> Unit,
    onIntensitySelected: (String) -> Unit,
    onLaunch: () -> Unit,
    onSurpriseMe: () -> Unit,
    onRollComplete: (Int) -> Unit,
    onSignInClick: () -> Unit,
    onGameMode: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxHeight < CompactHeight
        val sectionGap = if (isCompact) 8.dp else 16.dp
        val diceSize = (maxHeight * if (isCompact) 0.2f else 0.24f).coerceIn(120.dp, 240.dp)

        // Fills the screen when everything fits (SpaceBetween spreads the
        // sections) and scrolls when it doesn't — small phones, large font
        // scale — so the Launch button is never hidden behind the bottom bar.
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = sectionGap)
                .bottomBarClearance(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppLogo(inline = isCompact)
                if (streakDays >= 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StreakBadge(days = streakDays)
                }
                if (isAnonymous) {
                    Spacer(modifier = Modifier.height(10.dp))
                    SignInNudgeBanner(onClick = onSignInClick)
                }
            }

            RizzDice(
                rolling        = rolling,
                onRollComplete = onRollComplete,
                modifier       = Modifier.padding(vertical = sectionGap).size(diceSize)
            )

            Column(verticalArrangement = Arrangement.spacedBy(sectionGap)) {
                SelectorGroup(
                    title            = stringResource(Res.string.environment_label),
                    options          = environments,
                    selectedOption   = selectedEnvironment,
                    onOptionSelected = onEnvironmentSelected,
                    selectionColorProvider = { AppConstants.getEnvironmentColor(it) },
                    iconProvider           = { AppConstants.getEnvironmentIcon(it) }
                )
                SelectorGroup(
                    title            = stringResource(Res.string.intensity_label),
                    options          = intensities,
                    selectedOption   = selectedIntensity,
                    onOptionSelected = onIntensitySelected,
                    selectionColorProvider = { AppConstants.getIntensityColor(it) },
                    iconProvider           = { AppConstants.getIntensityIcon(it) },
                    isRestricted           = { option -> option == "int_spicy" && !isPremium }
                )
            }

            Column(
                modifier = Modifier.padding(top = sectionGap),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LaunchButton(
                    onClick          = onLaunch,
                    environmentColor = AppConstants.getEnvironmentColor(selectedEnvironment),
                    intensityColor   = AppConstants.getIntensityColor(selectedIntensity)
                )
                // Side by side so Game mode doesn't push the layout into scrolling
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick  = onSurpriseMe,
                        enabled  = !isLoading && !rolling,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape    = RoundedCornerShape(24.dp),
                        border   = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                    ) {
                        Text(
                            text       = "🎲 ${stringResource(Res.string.surprise_me_button)}",
                            color      = NeonCyan,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines   = 1
                        )
                    }
                    OutlinedButton(
                        onClick  = onGameMode,
                        enabled  = !isLoading && !rolling,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape    = RoundedCornerShape(24.dp),
                        border   = BorderStroke(1.dp, NeonPink.copy(alpha = 0.5f)),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = NeonPink)
                    ) {
                        Text(
                            text       = "🎉 ${stringResource(Res.string.game_entry)}",
                            color      = NeonPink,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines   = 1
                        )
                    }
                }
            }
        }
    }
}
