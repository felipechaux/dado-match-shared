package com.dadomatch.shared.feature.success.presentation.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.feature.success.domain.model.SuccessRecord
import com.dadomatch.shared.presentation.ui.components.EmptyState
import com.dadomatch.shared.presentation.ui.theme.DarkSurface
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.TextGray
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.presentation.viewmodel.SuccessesViewModel
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.env_bar
import com.dadomatch.shared.shared.generated.resources.env_beach
import com.dadomatch.shared.shared.generated.resources.env_cafe
import com.dadomatch.shared.shared.generated.resources.env_concert
import com.dadomatch.shared.shared.generated.resources.env_gym
import com.dadomatch.shared.shared.generated.resources.env_library
import com.dadomatch.shared.shared.generated.resources.env_online
import com.dadomatch.shared.shared.generated.resources.env_party
import com.dadomatch.shared.shared.generated.resources.env_work
import com.dadomatch.shared.shared.generated.resources.int_cringe
import com.dadomatch.shared.shared.generated.resources.int_direct
import com.dadomatch.shared.shared.generated.resources.int_funny
import com.dadomatch.shared.shared.generated.resources.int_romantic
import com.dadomatch.shared.shared.generated.resources.int_spicy
import com.dadomatch.shared.shared.generated.resources.loading
import com.dadomatch.shared.shared.generated.resources.no_successes_yet
import com.dadomatch.shared.shared.generated.resources.recent_label
import com.dadomatch.shared.shared.generated.resources.start_rolling
import com.dadomatch.shared.shared.generated.resources.success_subtitle
import com.dadomatch.shared.shared.generated.resources.success_title
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SuccessesScreen(
    onNavigateToPaywall: () -> Unit = {}
) {
    val viewModel: SuccessesViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val reversedSuccesses = remember(uiState.successes) { uiState.successes.reversed() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepDarkBlue)
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp)
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = stringResource(Res.string.no_successes_yet),
                            description = stringResource(Res.string.start_rolling),
                            icon = "🎲"
                        )
                    }
                }
                else -> {
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

            if (uiState.successes.isNotEmpty() && !uiState.isLoading) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(Res.string.recent_label),
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    itemsIndexed(
                        items = reversedSuccesses,
                        key = { _, record -> record.id }
                    ) { index, record ->
                        SuccessItem(
                            record = record,
                            index = index,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        // Premium restriction overlay
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
                    Text(text = "💎", fontSize = 48.sp)
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
                        onClick = onNavigateToPaywall,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Text("Upgrade to Pro", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessItem(
    record: SuccessRecord,
    index: Int,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 350)
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 48f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    val accentColor = if (record.wasSuccessful) NeonCyan else TextGray

    Box(
        modifier = modifier
            .graphicsLayer(alpha = alpha, translationY = translationY)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
    ) {
        // Neon left accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(accentColor)
        )

        Row(
            modifier = Modifier
                .padding(start = 15.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (record.wasSuccessful) "🔥" else "💀",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Full icebreaker text
                Text(
                    text = record.icebreaker,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Environment & intensity tags
                val envRes = when (record.environment) {
                    "env_gym"     -> Res.string.env_gym
                    "env_party"   -> Res.string.env_party
                    "env_bar"     -> Res.string.env_bar
                    "env_cafe"    -> Res.string.env_cafe
                    "env_beach"   -> Res.string.env_beach
                    "env_work"    -> Res.string.env_work
                    "env_online"  -> Res.string.env_online
                    "env_concert" -> Res.string.env_concert
                    "env_library" -> Res.string.env_library
                    else -> null
                }
                val intRes = when (record.intensity) {
                    "int_cringe"   -> Res.string.int_cringe
                    "int_romantic" -> Res.string.int_romantic
                    "int_direct"   -> Res.string.int_direct
                    "int_funny"    -> Res.string.int_funny
                    "int_spicy"    -> Res.string.int_spicy
                    else -> null
                }
                val envText = if (envRes != null) stringResource(envRes) else record.environment
                val intText = if (intRes != null) stringResource(intRes) else record.intensity

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TagChip(text = envText)
                    TagChip(text = intText, color = accentColor)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formatDate(record.date),
                    color = TextGray.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    color: androidx.compose.ui.graphics.Color = TextGray
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDate(instant: kotlin.time.Instant): String {
    val kx = Instant.fromEpochMilliseconds(instant.toEpochMilliseconds())
    val local = kx.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = local.month.name.take(3)
        .lowercase()
        .replaceFirstChar { it.uppercase() }
    return "$month ${local.dayOfMonth}, ${local.year}"
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
                val y = if (record.wasSuccessful) height * 0.2f else height * 0.8f
                Offset(x, y)
            }

            path.moveTo(points.first().x, points.first().y)
            points.forEach { point -> path.lineTo(point.x, point.y) }

            drawPath(
                path = path,
                brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPink)),
                style = Stroke(width = 3.dp.toPx())
            )

            points.forEach { point ->
                drawCircle(color = NeonPink, radius = 4.dp.toPx(), center = point)
            }
        }
    }
}
