package com.dadomatch.shared.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.home_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
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
        
        if (showTitle) {
            Text(
                text = stringResource(Res.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}
