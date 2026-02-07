package com.dadomatch.shared.presentation.ui.screens
 
import androidx.compose.ui.text.intl.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.presentation.ui.components.ActionChoiceDialog
import com.dadomatch.shared.presentation.ui.components.FeedbackDialog
import com.dadomatch.shared.presentation.ui.components.IcebreakerDialog
import com.dadomatch.shared.presentation.ui.components.RizzDice
import com.dadomatch.shared.presentation.ui.components.SelectorGroup
import com.dadomatch.shared.presentation.ui.theme.AppTheme
import com.dadomatch.shared.presentation.ui.theme.AppConstants
import com.dadomatch.shared.presentation.ui.theme.DarkSurface
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.presentation.viewmodel.HomeViewModel
import com.dadomatch.shared.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

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
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(DeepDarkBlue)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Logo & Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Static Dice Logo (Isometric feel)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val s = size.width
                        val padding = s * 0.1f
                        val h = s - padding * 2
                        
                        // Draw a simple isometric cube outline
                        val path = Path().apply {
                            moveTo(s/2, padding) // Top
                            lineTo(s-padding, s/3) // Right
                            lineTo(s-padding, 2*s/3) // Bottom Right
                            lineTo(s/2, s-padding) // Bottom
                            lineTo(padding, 2*s/3) // Bottom Left
                            lineTo(padding, s/3) // Top Left
                            close()
                        }
                        drawPath(path, color = NeonCyan, style = Stroke(width = 2.dp.toPx()))
                        
                        // Internal lines
                        drawLine(NeonCyan, Offset(s/2, padding), Offset(s/2, s/2), strokeWidth = 1.dp.toPx())
                        drawLine(NeonCyan, Offset(s/2, s/2), Offset(s-padding, s/3), strokeWidth = 1.dp.toPx())
                        drawLine(NeonCyan, Offset(s/2, s/2), Offset(padding, s/3), strokeWidth = 1.dp.toPx())
                        drawLine(NeonCyan, Offset(s/2, s/2), Offset(s/2, s-padding), strokeWidth = 1.dp.toPx())
                        
                        // Tiny pips
                        drawCircle(NeonPink, 1.5.dp.toPx(), Offset(s/3, s/2.5f))
                        drawCircle(NeonPink, 1.5.dp.toPx(), Offset(2*s/3, s/2.5f))
                        drawCircle(TextWhite, 2.dp.toPx(), Offset(s/2, 2*s/3))
                    }
                }
                Text(
                    text = stringResource(Res.string.home_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
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
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Environment Selector
            SelectorGroup(
                title = stringResource(Res.string.environment_label),
                options = environments,
                selectedOption = selectedEnvironment,
                onOptionSelected = { selectedEnvironment = it },
                selectionColorProvider = { AppConstants.getEnvironmentColor(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Intensity Selector
            SelectorGroup(
                title = stringResource(Res.string.intensity_label),
                options = intensities,
                selectedOption = selectedIntensity,
                onOptionSelected = { selectedIntensity = it },
                selectionColorProvider = { AppConstants.getIntensityColor(it) },
                isRestricted = { option -> option == "int_spicy" && !uiState.isPremium }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Lanzar Button
            Button(
                onClick = {
                    rolling = true
                    viewModel.dismissIcebreaker()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = NeonPink,
                        spotColor = NeonCyan
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    AppConstants.getEnvironmentColor(selectedEnvironment),
                                    AppConstants.getIntensityColor(selectedIntensity)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.launch_button),
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp)
                }
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

        if (uiState.showFeedbackDialog) {
            FeedbackDialog(
                onSubmit = { feedback -> viewModel.onSubmitFeedback(feedback) }
            )
        }

        if (uiState.error != null) {
            AlertDialog(
                onDismissRequest = { /* Handle error dismissal */ },
                confirmButton = {
                    TextButton(onClick = { /* Handle retry or dismiss */ }) {
                        Text(stringResource(Res.string.ok_button), color = NeonPink)
                    }
                },
                title = { Text(stringResource(Res.string.error_title), color = NeonPink) },
                text = { Text(uiState.error!!, color = TextWhite) },
                containerColor = DarkSurface
            )
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
