package com.dadomatch.shared.feature.icebreaker.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.presentation.ui.theme.DarkSurface
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.TextGray
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.feedback_desc
import com.dadomatch.shared.shared.generated.resources.feedback_rating_cringe
import com.dadomatch.shared.shared.generated.resources.feedback_rating_fire
import com.dadomatch.shared.shared.generated.resources.feedback_rating_good
import com.dadomatch.shared.shared.generated.resources.feedback_rating_meh
import com.dadomatch.shared.shared.generated.resources.feedback_skip_button
import com.dadomatch.shared.shared.generated.resources.feedback_submit_button
import com.dadomatch.shared.shared.generated.resources.how_did_it_go_title
import org.jetbrains.compose.resources.stringResource

private val RATING_EMOJIS = listOf(1 to "💀", 2 to "😐", 3 to "😊", 4 to "🔥")

/**
 * Full-screen overlay dialog for collecting icebreaker feedback.
 *
 * @param onDismiss Called when the user closes without submitting.
 * @param onSubmit  Called with a 1–4 rating and an optional comment string.
 */
@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }

    val ratingLabels = listOf(
        stringResource(Res.string.feedback_rating_cringe),
        stringResource(Res.string.feedback_rating_meh),
        stringResource(Res.string.feedback_rating_good),
        stringResource(Res.string.feedback_rating_fire)
    )

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
                .padding(horizontal = 28.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* consume */ }
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔥 ${stringResource(Res.string.how_did_it_go_title)}",
                color = NeonCyan,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.feedback_desc),
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // ── Emoji rating row ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RATING_EMOJIS.forEachIndexed { index, (value, emoji) ->
                    val isSelected = selectedRating == value
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) NeonCyan.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .clickable { selectedRating = value }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(emoji, fontSize = 32.sp)
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = ratingLabels[index],
                            color = if (isSelected) NeonCyan else TextGray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Submit ────────────────────────────────────────────────────
            Button(
                onClick = { if (selectedRating > 0) onSubmit(selectedRating, "") },
                enabled = selectedRating > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
            ) {
                Text(
                    text = stringResource(Res.string.feedback_submit_button),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(Res.string.feedback_skip_button), color = TextGray)
            }
        }
    }
}
