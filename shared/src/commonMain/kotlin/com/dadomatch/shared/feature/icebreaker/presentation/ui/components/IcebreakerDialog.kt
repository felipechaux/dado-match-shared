package com.dadomatch.shared.feature.icebreaker.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.presentation.ui.theme.DarkSurface
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonMint
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.TextGray
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.copied_button
import com.dadomatch.shared.shared.generated.resources.copy_button
import com.dadomatch.shared.shared.generated.resources.got_it_button
import com.dadomatch.shared.shared.generated.resources.how_did_it_go_title
import com.dadomatch.shared.shared.generated.resources.icebreaker_title
import com.dadomatch.shared.shared.generated.resources.reroll_button
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

/**
 * Flexible full-screen overlay dialog used for icebreakers, errors, and paywall nudges.
 *
 * - Standard icebreaker: provide [onReroll] and/or [onFeedback] for extra actions.
 * - Error / paywall: provide [customButton] to replace all action buttons with a custom slot.
 */
@Composable
fun IcebreakerDialog(
    icebreakerText: String,
    onDismiss: () -> Unit,
    title: String? = null,
    onFeedback: (() -> Unit)? = null,
    onReroll: (() -> Unit)? = null,
    customButton: (@Composable () -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1800)
            copied = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* consume tap — card should not close on inner tap */ }
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎲 ${title ?: stringResource(Res.string.icebreaker_title)}",
                color = NeonCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = icebreakerText,
                color = TextWhite,
                fontSize = 20.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            if (customButton != null) {
                // Custom button slot (error, paywall, etc.)
                customButton()
            } else {
                // ── Copy to clipboard ─────────────────────────────────────
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(icebreakerText))
                        copied = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, if (copied) NeonMint else NeonCyan.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (copied) "✓ ${stringResource(Res.string.copied_button)}"
                        else "📋 ${stringResource(Res.string.copy_button)}",
                        color = if (copied) NeonMint else NeonCyan
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ── Got It ────────────────────────────────────────────────
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                ) {
                    Text(
                        text = stringResource(Res.string.got_it_button),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ── Reroll ────────────────────────────────────────────────
                if (onReroll != null) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = onReroll,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎲 ${stringResource(Res.string.reroll_button)}",
                            color = TextGray
                        )
                    }
                }

                // ── Feedback ──────────────────────────────────────────────
                if (onFeedback != null) {
                    TextButton(
                        onClick = onFeedback,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "🔥 ${stringResource(Res.string.how_did_it_go_title)}", color = TextGray, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
