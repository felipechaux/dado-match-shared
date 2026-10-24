package com.dadomatch.shared.feature.icebreaker.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.haptic.rememberHapticEngine
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.cd_roll_dice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.dadomatch.shared.presentation.ui.theme.AppTheme
import kotlin.math.*

@Composable
fun RizzApp() {
    RizzRoller()
}

@Composable
fun RizzRoller(modifier: Modifier = Modifier) {
    var result by remember { mutableStateOf(1) }
    var rolling by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RizzDice(
            rolling = rolling,
            onRollComplete = {
                result = it
                rolling = false
            },
            modifier = Modifier.size(250.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { rolling = true }) {
            Text(text = "Rolar", fontSize = 24.sp)
        }
    }
}

private val ReferenceBoxSize = 250.dp
private const val DiceFraction = 0.8f // the cube canvas was 200dp of the 250dp box

// Rotation (x, y) that turns each face toward the viewer, plus a small tilt so
// the landed dice still reads as a cube (two side faces peek out)
private val FaceRotations = mapOf(
    1 to (0f to 0f),
    2 to (0f to -90f),
    3 to (-90f to 0f),
    4 to (90f to 0f),
    5 to (0f to 90f),
    6 to (0f to 180f),
)
private const val RestTiltX = -18f
private const val RestTiltY = 24f

// Pause after landing before the idle drift starts, so the result can be read
private const val RestAfterLandingMs = 1800L
private const val IdleDegreesPerSecondX = 18f
private const val IdleDegreesPerSecondY = 27f

/**
 * Neon 3D dice. Flip [rolling] to true to throw it: it jumps, tumbles, bounces
 * a few times with a shadow that follows the height, lands on a face and reports
 * that face through [onRollComplete].
 *
 * With [onTap] set the dice itself is a button: it squashes while pressed and
 * the tap should start a roll (same as the Launch button).
 */
@Composable
fun RizzDice(
    rolling: Boolean,
    onRollComplete: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
    tapEnabled: Boolean = true,
) {
    val haptic = rememberHapticEngine()
    val infiniteTransition = rememberInfiniteTransition()

    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Height above the floor in reference dp (negative = up)
    val jumpOffset = remember { Animatable(0f) }
    // Landing squash: 1 = round, < 1 = flattened against the floor
    val squash = remember { Animatable(1f) }
    val rollX = remember { Animatable(RestTiltX + 20f) }
    val rollY = remember { Animatable(RestTiltY + 20f) }
    val rollZ = remember { Animatable(0f) }

    // Slow spin while idle; it starts from wherever the last roll landed, so the
    // dice never snaps back to a different pose
    var idleX by remember { mutableFloatStateOf(0f) }
    var idleY by remember { mutableFloatStateOf(0f) }
    var hasRolled by remember { mutableStateOf(false) }

    LaunchedEffect(rolling) {
        if (!rolling) {
            if (hasRolled) delay(RestAfterLandingMs)
            var last = withFrameNanos { it }
            while (true) {
                withFrameNanos { now ->
                    val dt = (now - last) / 1_000_000_000f
                    last = now
                    idleX += IdleDegreesPerSecondX * dt
                    idleY += IdleDegreesPerSecondY * dt
                }
            }
        }

        hasRolled = true
        // Fold the idle drift into the roll so the throw starts from the visible pose
        rollX.snapTo(rollX.value + idleX)
        rollY.snapTo(rollY.value + idleY)
        idleX = 0f
        idleY = 0f

        val result = (1..6).random()
        val (faceX, faceY) = FaceRotations.getValue(result)
        // Whole turns past the current angle, then the face offset: ends exactly on the face
        fun landing(current: Float, face: Float, spins: Int) =
            (floor(current / 360f) + spins) * 360f + face
        val targetX = landing(rollX.value, faceX + RestTiltX, 3 + (0..1).random())
        val targetY = landing(rollY.value, faceY + RestTiltY, 3 + (0..1).random())
        val targetZ = landing(rollZ.value, 0f, 2) + 90f * (0..3).random()

        // Throw → three bounces, each lower and shorter, like a real dice on a table
        val bounces = listOf(
            -95f to 340,  // the throw
            -40f to 230,
            -14f to 150,
            -4f to 90,
        )
        val tumbleMs = bounces.sumOf { it.second * 2 } - 120

        val tumble = launch {
            launch { rollX.animateTo(targetX, tween(tumbleMs, easing = FastOutSlowInEasing)) }
            launch { rollY.animateTo(targetY, tween(tumbleMs, easing = FastOutSlowInEasing)) }
            launch { rollZ.animateTo(targetZ, tween(tumbleMs, easing = LinearOutSlowInEasing)) }
        }

        haptic.medium()
        // Wind-up squash before leaving the floor
        squash.animateTo(0.88f, tween(70))
        bounces.forEachIndexed { i, (height, halfMs) ->
            launch { squash.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)) }
            jumpOffset.animateTo(height, tween(halfMs, easing = LinearOutSlowInEasing))
            jumpOffset.animateTo(0f, tween(halfMs, easing = FastOutLinearInEasing))
            // Impact: harder hits squash more and buzz stronger
            if (i == 0) haptic.heavy() else haptic.light()
            squash.snapTo(1f - 0.12f * (height / bounces.first().first))
        }
        squash.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow))
        tumble.join()
        haptic.success()
        onRollComplete(result)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && tapEnabled && !rolling) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)
    )
    val tapModifier = if (onTap != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = tapEnabled && !rolling,
            role = Role.Button,
            onClickLabel = stringResource(Res.string.cd_roll_dice),
            onClick = onTap
        )
    } else Modifier

    // Everything below is drawn in a 250dp reference box and scaled to the
    // actual size, so a smaller dice (short screens) never spills over its
    // neighbours. 250dp stays the default when the caller sets no size.
    Box(
        modifier = modifier.size(ReferenceBoxSize).then(tapModifier),
        contentAlignment = Alignment.Center
    ) {
        // Floor glow = the dice's shadow: shrinks and fades as it rises
        Canvas(modifier = Modifier.fillMaxSize()) {
            val k = size.minDimension / ReferenceBoxSize.toPx()
            val lift = (-jumpOffset.value / 95f).coerceIn(0f, 1f)
            val shadowScale = 1f - 0.45f * lift
            val ringWidth = 140.dp.toPx() * k * shadowScale
            val ringHeight = 35.dp.toPx() * k * shadowScale
            val centerY = center.y + 50.dp.toPx() * k

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(NeonPink.copy(alpha = 0.2f + 0.15f * (1f - lift)), Color.Transparent),
                    center = Offset(center.x, centerY),
                    radius = ringWidth / 2
                ),
                topLeft = Offset(center.x - ringWidth / 2, centerY - ringHeight / 2),
                size = Size(ringWidth, ringHeight)
            )

            drawOval(
                color = NeonCyan.copy(alpha = 0.4f * (1f - 0.6f * lift)),
                topLeft = Offset(center.x - (ringWidth * 0.8f) / 2, centerY - (ringHeight * 0.6f) / 2),
                size = Size(ringWidth * 0.8f, ringHeight * 0.6f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }

        // Depth-Sorted Face Renderer
        Canvas(
            modifier = Modifier
                .fillMaxSize(DiceFraction)
                // Jump, squash and press are applied in the draw layer so the
                // animation doesn't recompose
                .graphicsLayer {
                    translationY = jumpOffset.value.dp.toPx() * (size.height / (ReferenceBoxSize * DiceFraction).toPx())
                    scaleX = pressScale * (2f - squash.value)
                    scaleY = pressScale * squash.value
                    // Squash against the floor, not around the middle
                    transformOrigin = TransformOrigin(0.5f, 0.85f)
                }
        ) {
            val k = size.minDimension / (ReferenceBoxSize * DiceFraction).toPx()
            drawDepthSortedDice(
                // Half the box: even rotated corner-on (edge × √3) it stays inside
                size = 125.dp.toPx() * k,
                rotX = rollX.value + idleX,
                rotY = rollY.value + idleY,
                rotZ = rollZ.value
            )
        }

        // Particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val k = size.minDimension / ReferenceBoxSize.toPx()
            repeat(15) { i ->
                val angle = (i.toFloat() / 15) * 2 * PI.toFloat() + particleProgress * 2 * PI.toFloat()
                val r = (112.dp.toPx() + sin(particleProgress * 10 * PI.toFloat() + i) * 20) * k
                drawCircle(
                    color = if (i % 2 == 0) NeonCyan.copy(alpha = 0.3f) else NeonPink.copy(alpha = 0.3f),
                    radius = 2f,
                    center = Offset(center.x + cos(angle) * r, center.y + sin(angle) * r)
                )
            }
        }
    }
}

private fun DrawScope.drawDepthSortedDice(size: Float, rotX: Float, rotY: Float, rotZ: Float) {
    val h = size / 2
    
    // Define the 6 faces with their pips in local 3D coordinates
    // Each face has: original normal, corners IN LOCAL SPACE, and pip 3D positions IN LOCAL SPACE
    val faces = listOf(
        // Front (1) - normal pointing +Z
        FaceWithPips(
            normal = floatArrayOf(0f, 0f, 1f),
            corners = listOf(floatArrayOf(-h, -h, h), floatArrayOf(h, -h, h), floatArrayOf(h, h, h), floatArrayOf(-h, h, h)),
            pipPositions = listOf(floatArrayOf(0f, 0f, h)) // 1 pip in center
        ),
        // Back (6) - normal pointing -Z
        FaceWithPips(
            normal = floatArrayOf(0f, 0f, -1f),
            corners = listOf(floatArrayOf(h, -h, -h), floatArrayOf(-h, -h, -h), floatArrayOf(-h, h, -h), floatArrayOf(h, h, -h)),
            pipPositions = listOf(
                floatArrayOf(-h * 0.5f, -h * 0.5f, -h),
                floatArrayOf(-h * 0.5f, 0f, -h),
                floatArrayOf(-h * 0.5f, h * 0.5f, -h),
                floatArrayOf(h * 0.5f, -h * 0.5f, -h),
                floatArrayOf(h * 0.5f, 0f, -h),
                floatArrayOf(h * 0.5f, h * 0.5f, -h)
            ) // 6 pips in 2 columns
        ),
        // Right (2) - normal pointing +X
        FaceWithPips(
            normal = floatArrayOf(1f, 0f, 0f),
            corners = listOf(floatArrayOf(h, -h, h), floatArrayOf(h, -h, -h), floatArrayOf(h, h, -h), floatArrayOf(h, h, h)),
            pipPositions = listOf(
                floatArrayOf(h, -h * 0.35f, -h * 0.35f),
                floatArrayOf(h, h * 0.35f, h * 0.35f)
            ) // 2 pips diagonal
        ),
        // Left (5) - normal pointing -X
        FaceWithPips(
            normal = floatArrayOf(-1f, 0f, 0f),
            corners = listOf(floatArrayOf(-h, -h, -h), floatArrayOf(-h, -h, h), floatArrayOf(-h, h, h), floatArrayOf(-h, h, -h)),
            pipPositions = listOf(
                floatArrayOf(-h, -h * 0.35f, -h * 0.35f),
                floatArrayOf(-h, h * 0.35f, h * 0.35f),
                floatArrayOf(-h, -h * 0.35f, h * 0.35f),
                floatArrayOf(-h, h * 0.35f, -h * 0.35f),
                floatArrayOf(-h, 0f, 0f)
            ) // 5 pips (4 corners + center)
        ),
        // Top (3) - normal pointing -Y
        FaceWithPips(
            normal = floatArrayOf(0f, -1f, 0f),
            corners = listOf(floatArrayOf(-h, -h, -h), floatArrayOf(h, -h, -h), floatArrayOf(h, -h, h), floatArrayOf(-h, -h, h)),
            pipPositions = listOf(
                floatArrayOf(-h * 0.35f, -h, -h * 0.35f),
                floatArrayOf(0f, -h, 0f),
                floatArrayOf(h * 0.35f, -h, h * 0.35f)
            ) // 3 pips diagonal
        ),
        // Bottom (4) - normal pointing +Y
        FaceWithPips(
            normal = floatArrayOf(0f, 1f, 0f),
            corners = listOf(floatArrayOf(-h, h, h), floatArrayOf(h, h, h), floatArrayOf(h, h, -h), floatArrayOf(-h, h, -h)),
            pipPositions = listOf(
                floatArrayOf(-h * 0.35f, h, -h * 0.35f),
                floatArrayOf(-h * 0.35f, h, h * 0.35f),
                floatArrayOf(h * 0.35f, h, -h * 0.35f),
                floatArrayOf(h * 0.35f, h, h * 0.35f)
            ) // 4 pips in corners
        )
    )

    // Rotate all faces and their pips
    val rotatedFaces = faces.map { face ->
        val rotatedNormal = rotate3D(face.normal[0], face.normal[1], face.normal[2], rotX, rotY, rotZ)
        val rotatedCorners = face.corners.map { c -> rotate3D(c[0], c[1], c[2], rotX, rotY, rotZ) }
        val rotatedPips = face.pipPositions.map { pip -> rotate3D(pip[0], pip[1], pip[2], rotX, rotY, rotZ) }
        val avgZ = rotatedCorners.map { it[2] }.average().toFloat()
        
        RotatedFace(rotatedNormal, rotatedCorners, rotatedPips, avgZ)
    }

    // Sort by depth (back to front)
    val sortedFaces = rotatedFaces.sortedBy { it.avgZ }

    sortedFaces.forEach { face ->
        // Draw face with slight opacity to occlude back edges
        val path = Path().apply {
            face.corners.forEachIndexed { i, c ->
                val p = projectToScreen(c, size)
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
            close()
        }
        
        // Solid fill (very dark blue) to act as depth mask
        drawPath(path, color = Color(0xFF07070A), alpha = 0.95f)
        
        // Wireframe edges
        face.corners.forEachIndexed { i, c ->
            val start = projectToScreen(c, size)
            val end = projectToScreen(face.corners[(i + 1) % 4], size)
            drawLine(
                brush = Brush.linearGradient(listOf(NeonCyan, NeonPink)),
                start = start,
                end = end,
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        // Draw pips only if face is facing camera
        if (face.normal[2] > 0) {
            face.pipPositions.forEach { pip3D ->
                val screenPos = projectToScreen(pip3D, size)
                val zM = (pip3D[2] + size * 2) / (size * 2)
                
                drawCircle(
                    color = NeonCyan,
                    radius = 4.dp.toPx() * zM,
                    center = screenPos
                )
            }
        }
    }
}

private data class FaceWithPips(val normal: FloatArray, val corners: List<FloatArray>, val pipPositions: List<FloatArray>)
private data class RotatedFace(val normal: FloatArray, val corners: List<FloatArray>, val pipPositions: List<FloatArray>, val avgZ: Float)

private fun DrawScope.projectToScreen(p: FloatArray, size: Float): Offset {
    val zMod = (p[2] + size * 2) / (size * 2)
    return Offset(center.x + p[0] * zMod, center.y + p[1] * zMod)
}

private fun rotate3D(x: Float, y: Float, z: Float, rotX: Float, rotY: Float, rotZ: Float): FloatArray {
    val rx = rotX * PI.toFloat() / 180f
    val ry = rotY * PI.toFloat() / 180f
    val rz = rotZ * PI.toFloat() / 180f

    var px = x
    var py = y
    var pz = z

    // X
    var ty = py * cos(rx) - pz * sin(rx)
    var tz = py * sin(rx) + pz * cos(rx)
    py = ty
    pz = tz

    // Y
    var tx = px * cos(ry) + pz * sin(ry)
    tz = -px * sin(ry) + pz * cos(ry)
    px = tx
    pz = tz

    // Z
    tx = px * cos(rz) - py * sin(rz)
    ty = px * sin(rz) + py * cos(rz)
    px = tx
    py = ty

    return floatArrayOf(px, py, pz)
}

@Preview
@Composable
fun RizzAppPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue),
            contentAlignment = Alignment.Center
        ) {
            RizzApp()
        }
    }
}
