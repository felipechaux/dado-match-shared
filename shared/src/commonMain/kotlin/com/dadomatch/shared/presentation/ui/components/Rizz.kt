package com.dadomatch.shared.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import kotlinx.coroutines.launch
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

@Composable
fun RizzDice(
    rolling: Boolean,
    onRollComplete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val baseRotX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val baseRotY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val jumpOffset = remember { Animatable(0f) }
    val rollX = remember { Animatable(0f) }
    val rollY = remember { Animatable(0f) }
    val rollZ = remember { Animatable(0f) }

    LaunchedEffect(rolling) {
        if (rolling) {
            val targetX = rollX.value + (360f * 4) + (90f * (0..3).random())
            val targetY = rollY.value + (360f * 4) + (90f * (0..3).random())
            val targetZ = rollZ.value + (360f * 4) + (90f * (0..3).random())
            
            launch {
                jumpOffset.animateTo(-60f, tween(200, easing = FastOutSlowInEasing))
                jumpOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
            }
            
            launch {
                rollX.animateTo(targetX, tween(2500, easing = FastOutSlowInEasing))
            }
            launch {
                rollY.animateTo(targetY, tween(2500, easing = FastOutSlowInEasing))
            }
            launch {
                rollZ.animateTo(targetZ, tween(2500, easing = FastOutSlowInEasing))
                onRollComplete((1..6).random())
            }
        }
    }

    Box(
        modifier = modifier.size(250.dp),
        contentAlignment = Alignment.Center
    ) {
        // Base Glow Rings (Matches image)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ringWidth = 140.dp.toPx()
            val ringHeight = 35.dp.toPx()
            val centerY = center.y + 50.dp.toPx()
            
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(NeonPink.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(center.x, centerY),
                    radius = ringWidth / 2
                ),
                topLeft = Offset(center.x - ringWidth / 2, centerY - ringHeight / 2),
                size = Size(ringWidth, ringHeight)
            )
            
            drawOval(
                color = NeonCyan.copy(alpha = 0.4f),
                topLeft = Offset(center.x - (ringWidth * 0.8f) / 2, centerY - (ringHeight * 0.6f) / 2),
                size = Size(ringWidth * 0.8f, ringHeight * 0.6f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }

        // Depth-Sorted Face Renderer
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .offset(y = jumpOffset.value.dp)
        ) {
            val currentX = if (rolling) rollX.value else baseRotX + 20f
            val currentY = if (rolling) rollY.value else baseRotY + 20f
            val currentZ = if (rolling) rollZ.value else 10f

            drawDepthSortedDice(
                size = 100.dp.toPx(),
                rotX = currentX,
                rotY = currentY,
                rotZ = currentZ
            )
        }

        // Particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(15) { i ->
                val angle = (i.toFloat() / 15) * 2 * PI.toFloat() + particleProgress * 2 * PI.toFloat()
                val r = 100.dp.toPx() + sin(particleProgress * 10 * PI.toFloat() + i) * 20
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
