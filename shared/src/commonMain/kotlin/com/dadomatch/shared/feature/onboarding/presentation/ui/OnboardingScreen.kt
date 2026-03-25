package com.dadomatch.shared.feature.onboarding.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.RizzDice
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.TextGray
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.onboarding_1_desc
import com.dadomatch.shared.shared.generated.resources.onboarding_1_title
import com.dadomatch.shared.shared.generated.resources.onboarding_2_desc
import com.dadomatch.shared.shared.generated.resources.onboarding_2_title
import com.dadomatch.shared.shared.generated.resources.onboarding_3_desc
import com.dadomatch.shared.shared.generated.resources.onboarding_3_title
import com.dadomatch.shared.shared.generated.resources.onboarding_back
import com.dadomatch.shared.shared.generated.resources.onboarding_button
import com.dadomatch.shared.shared.generated.resources.onboarding_next
import org.jetbrains.compose.resources.stringResource

private data class OnboardingPage(
    val emoji: String,
    val titleRes: org.jetbrains.compose.resources.StringResource,
    val descRes: org.jetbrains.compose.resources.StringResource,
    val showDice: Boolean = false
)

@Composable
fun OnboardingScreen(onDismiss: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 3

    val pages = listOf(
        OnboardingPage("🎲", Res.string.onboarding_1_title, Res.string.onboarding_1_desc, showDice = true),
        OnboardingPage("🌍", Res.string.onboarding_2_title, Res.string.onboarding_2_desc),
        OnboardingPage("🚀", Res.string.onboarding_3_title, Res.string.onboarding_3_desc),
    )

    // Full-screen box that consumes all touches — nothing behind is reachable
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepDarkBlue.copy(alpha = 0.97f))
            .pointerInput(Unit) { /* consume all touch events */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            // ── Page dots ─────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalSteps) { index ->
                    val isActive = index == currentStep
                    val width by animateFloatAsState(
                        targetValue = if (isActive) 24f else 8f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = width.dp, height = 8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) NeonCyan else TextGray.copy(alpha = 0.4f))
                    )
                }
            }

            // ── Animated page content ─────────────────────────────────────────
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val forward = targetState > initialState
                    val enterOffset = if (forward) 1 else -1
                    val exitOffset  = if (forward) -1 else 1
                    (slideInHorizontally(tween(300)) { it / 2 * enterOffset } + fadeIn(tween(300))) togetherWith
                    (slideOutHorizontally(tween(300)) { it / 2 * exitOffset } + fadeOut(tween(200)))
                }
            ) { step ->
                val page = pages[step]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (page.showDice) {
                        RizzDice(
                            rolling = false,
                            onRollComplete = {},
                            modifier = Modifier.size(180.dp)
                        )
                    } else {
                        Text(
                            text = page.emoji,
                            fontSize = 80.sp,
                            modifier = Modifier.size(140.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(page.titleRes),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(page.descRes),
                        fontSize = 16.sp,
                        color = TextWhite.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            // ── Next / Start button ───────────────────────────────────────────
            Button(
                onClick = {
                    if (currentStep < totalSteps - 1) currentStep++
                    else onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = androidx.compose.foundation.layout.PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPink)),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentStep < totalSteps - 1)
                            stringResource(Res.string.onboarding_next)
                        else
                            stringResource(Res.string.onboarding_button),
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Back button — only visible after step 1 ───────────────────────
            AnimatedVisibility(
                visible = currentStep > 0,
                enter = fadeIn(tween(200)),
                exit  = fadeOut(tween(200))
            ) {
                TextButton(onClick = { currentStep-- }) {
                    Text(
                        text = stringResource(Res.string.onboarding_back),
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
