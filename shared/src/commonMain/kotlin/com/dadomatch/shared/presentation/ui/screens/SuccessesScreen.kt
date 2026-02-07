package com.dadomatch.shared.presentation.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.domain.model.SuccessRecord
import com.dadomatch.shared.presentation.ui.theme.DarkSurface
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.TextGray
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.presentation.viewmodel.SuccessesViewModel
import com.dadomatch.shared.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SuccessesScreen() {
    val viewModel: SuccessesViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepDarkBlue)
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(Res.string.success_title),
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(Res.string.success_subtitle),
                color = TextGray,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                uiState.isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NeonCyan)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(Res.string.loading),
                                color = TextGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                uiState.successes.isEmpty() -> {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = "🎲",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(Res.string.no_successes_yet),
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(Res.string.start_rolling),
                                color = TextGray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        }
                    }
                }
                else -> {
                    // Progress Graphic - only show when there's data
                    ProgressGraphic(
                        successes = uiState.successes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface)
                            .padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Only show recent label and list when there's data
            if (uiState.successes.isNotEmpty() && !uiState.isLoading) {
                Text(
                    text = stringResource(Res.string.recent_label),
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.successes.reversed()) { record ->
                        SuccessItem(record)
                    }
                }
            }
        }
        
        // Premium Restriction Overlay
        if (uiState.isRestricted && uiState.successes.size > 3) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepDarkBlue.copy(alpha = 0.7f))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkSurface)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "💎",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "PRO ANALYTICS",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonCyan,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Unlock detailed success history and performance charts with DadoMatch Pro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { /* Navigation to Paywall will be handled via a callback or Navigator if available */ },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPink
                        )
                    ) {
                        Text("Upgrade to Pro", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressGraphic(
    successes: List<SuccessRecord>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (successes.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val spacing = width / (successes.size.coerceAtLeast(2) - 1).coerceAtLeast(1)
            
            val path = Path()
            val points = successes.mapIndexed { index, record ->
                val x = index * spacing
                // Map success (true/false) to height
                val y = if (record.wasSuccessful) height * 0.2f else height * 0.8f
                Offset(x, y)
            }

            path.moveTo(points.first().x, points.first().y)
            points.forEach { point ->
                path.lineTo(point.x, point.y)
            }

            drawPath(
                path = path,
                brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPink)),
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw points
            points.forEach { point ->
                drawCircle(
                    color = NeonPink,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

@Composable
fun SuccessItem(record: SuccessRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (record.wasSuccessful) NeonCyan.copy(alpha = 0.2f) else TextGray.copy(alpha = 0.2f),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(if (record.wasSuccessful) "🔥" else "💀")
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.icebreaker,
                color = TextWhite,
                fontSize = 14.sp,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
            val envRes = when(record.environment) {
                "env_gym" -> Res.string.env_gym
                "env_party" -> Res.string.env_party
                "env_library" -> Res.string.env_library
                "env_cafe" -> Res.string.env_cafe
                else -> null
            }
            val intRes = when(record.intensity) {
                "int_cringe" -> Res.string.int_cringe
                "int_romantic" -> Res.string.int_romantic
                "int_direct" -> Res.string.int_direct
                "int_funny" -> Res.string.int_funny
                else -> null
            }
            
            val envText = if (envRes != null) stringResource(envRes) else record.environment
            val intText = if (intRes != null) stringResource(intRes) else record.intensity
            
            Text(
                text = "$envText • $intText",
                color = TextGray,
                fontSize = 12.sp
            )
        }
    }
}
