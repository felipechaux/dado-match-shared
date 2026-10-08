package com.dadomatch.shared.feature.icebreaker.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.home_streak
import org.jetbrains.compose.resources.stringResource

private val StreakOrange = Color(0xFFFF8A3D)

/** "🔥 4-day streak" pill under the logo. */
@Composable
fun StreakBadge(days: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Text(
        text = stringResource(Res.string.home_streak).replace("%d", days.toString()),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = StreakOrange,
        modifier = modifier
            .clip(shape)
            .background(StreakOrange.copy(alpha = 0.12f))
            .border(1.dp, StreakOrange.copy(alpha = 0.35f), shape)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}
