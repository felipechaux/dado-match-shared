package com.dadomatch.shared.feature.onboarding.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.RizzDice
import com.dadomatch.shared.presentation.haptic.rememberHapticEngine
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.NeonPurple
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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class OnboardingPage(
    val titleRes: StringResource,
    val descRes: StringResource,
    val accent: Color,
)

@Composable
fun OnboardingScreen(onDismiss: () -> Unit) {
    val haptic = rememberHapticEngine()

    var currentStep by remember { mutableIntStateOf(0) }
    // Drives the dice: flipped to `true` on every advance so the cube re-rolls and
    // fires its own land-haptic (medium throw → light bounces → heavy land → success).
    var rolling by remember { mutableStateOf(false) }

    val pages = remember {
        listOf(
            OnboardingPage(Res.string.onboarding_1_title, Res.string.onboarding_1_desc, NeonCyan),
            OnboardingPage(Res.string.onboarding_2_title, Res.string.onboarding_2_desc, NeonPink),
            OnboardingPage(Res.string.onboarding_3_title, Res.string.onboarding_3_desc, NeonPurple),
        )
    }
    val totalSteps = pages.size
    val isLast = currentStep == totalSteps - 1

    // Accent color glides between steps and tints the glow + page dots.
    val accent by animateColorAsState(
        targetValue = pages[currentStep].accent,
        animationSpec = tween(500),
    )

    // Grand entrance: the dice rolls once as the hero lands on screen.
    LaunchedEffect(Unit) { rolling = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepDarkBlue.copy(alpha = 0.98f))
            .pointerInput(Unit) { /* consume all touches — nothing behind is reachable */ },
        contentAlignment = Alignment.Center,
    ) {
        // ── Pulsing neon glow behind the dice ─────────────────────────────────
        PulsingGlow(color = accent, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            // ── Page dots ─────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalSteps) { index ->
                    val isActive = index == currentStep
                    val width by animateFloatAsState(
                        targetValue = if (isActive) 24f else 8f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    )
                    Box(
                        modifier = Modifier
                            .size(width = width.dp, height = 8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) accent else TextGray.copy(alpha = 0.35f)),
                    )
                }
            }

            // ── Kinetic dice hero (persistent across steps) ─────────────────────
            RizzDice(
                rolling = rolling,
                onRollComplete = { rolling = false },
                modifier = Modifier.size(220.dp),
            )

            // ── Animated title + description ────────────────────────────────────
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val forward = targetState > initialState
                    val enter = if (forward) 1 else -1
                    val exit = if (forward) -1 else 1
                    (slideInHorizontally(tween(320)) { it / 2 * enter } + fadeIn(tween(320))) togetherWith
                        (slideOutHorizontally(tween(320)) { it / 2 * exit } + fadeOut(tween(200)))
                },
            ) { step ->
                val page = pages[step]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(page.titleRes),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(page.descRes),
                        fontSize = 16.sp,
                        color = TextWhite.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                    )
                }
            }

            // ── Primary CTA: NEXT (rolls the dice) / LET'S START (dismiss) ───────
            Button(
                onClick = {
                    if (isLast) {
                        haptic.heavy()          // celebratory finish
                        onDismiss()
                    } else {
                        haptic.medium()         // advance tick
                        currentStep++
                        rolling = true          // re-roll into the next step
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPink)),
                            shape = RoundedCornerShape(28.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(if (isLast) Res.string.onboarding_button else Res.string.onboarding_next),
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // ── Back — only after step 1 ────────────────────────────────────────
            AnimatedVisibility(
                visible = currentStep > 0,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
            ) {
                TextButton(
                    onClick = {
                        haptic.light()
                        currentStep--
                        rolling = true
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.onboarding_back),
                        color = TextGray,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

/** A soft neon halo that breathes behind the dice, tinted by the current step's accent. */
@Composable
private fun PulsingGlow(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val pulse by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Canvas(modifier = modifier) {
        val radius = size.minDimension * 0.42f * pulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.28f), Color.Transparent),
                center = Offset(center.x, center.y - size.minDimension * 0.06f),
                radius = radius,
            ),
            radius = radius,
            center = Offset(center.x, center.y - size.minDimension * 0.06f),
        )
    }
}
