package com.dadomatch.shared.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.dadomatch.shared.presentation.ui.theme.AppTheme
import kotlin.math.*

@Composable
fun Rizz(
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
    
    // Define the 6 faces
    val faces = listOf(
        // Front (1)
        RawFace(floatArrayOf(0f, 0f, 1f), listOf(floatArrayOf(-h, -h, h), floatArrayOf(h, -h, h), floatArrayOf(h, h, h), floatArrayOf(-h, h, h)), listOf(0f to 0f)),
        // Back (6)
        RawFace(floatArrayOf(0f, 0f, -1f), listOf(floatArrayOf(h, -h, -h), floatArrayOf(-h, -h, -h), floatArrayOf(-h, h, -h), floatArrayOf(h, h, -h)), listOf(-0.5f to -0.5f, -0.5f to 0f, -0.5f to 0.5f, 0.5f to -0.5f, 0.5f to 0f, 0.5f to 0.5f)),
        // Right (2)
        RawFace(floatArrayOf(1f, 0f, 0f), listOf(floatArrayOf(h, -h, h), floatArrayOf(h, -h, -h), floatArrayOf(h, h, -h), floatArrayOf(h, h, h)), listOf(-0.35f to -0.35f, 0.35f to 0.35f)),
        // Left (5)
        RawFace(floatArrayOf(-1f, 0f, 0f), listOf(floatArrayOf(-h, -h, -h), floatArrayOf(-h, -h, h), floatArrayOf(-h, h, h), floatArrayOf(-h, h, -h)), listOf(-0.35f to -0.35f, 0.35f to 0.35f, -0.35f to 0.35f, 0.35f to -0.35f, 0f to 0f)),
        // Top (3)
        RawFace(floatArrayOf(0f, -1f, 0f), listOf(floatArrayOf(-h, -h, -h), floatArrayOf(h, -h, -h), floatArrayOf(h, -h, h), floatArrayOf(-h, -h, h)), listOf(-0.35f to -0.35f, 0f to 0f, 0.35f to 0.35f)),
        // Bottom (4)
        RawFace(floatArrayOf(0f, 1f, 0f), listOf(floatArrayOf(-h, h, h), floatArrayOf(h, h, h), floatArrayOf(h, h, -h), floatArrayOf(-h, h, -h)), listOf(-0.35f to -0.35f, -0.35f to 0.35f, 0.35f to -0.35f, 0.35f to 0.35f))
    )

    // Calculate rotated faces
    val rotatedFaces = faces.map { face ->
        val rotatedNormal = rotate3D(face.normal[0], face.normal[1], face.normal[2], rotX, rotY, rotZ)
        val rotatedCorners = face.corners.map { c -> rotate3D(c[0], c[1], c[2], rotX, rotY, rotZ) }
        val avgZ = rotatedCorners.map { it[2] }.average().toFloat()
        
        ProcessedFace(rotatedNormal, rotatedCorners, face.pips, avgZ)
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

        // Pips (if face is facing camera or at least near enough)
        // Note: With solid face fills, we can actually draw all sorted faces,
        // but backface culling (normal.z > 0) is still safer for pips.
        if (face.normal[2] > 0) {
            face.pips.forEach { (px, py) ->
                // Pips are on the local XY plane of the rotated face
                // We calculate their 3D world pos by interpolating corners
                // Local coordinate px, py range [-0.5, 0.5] if corners are [-h, h]
                // But corners are already rotated. Better calculate world pos THEN rotate.
                
                // Simplified: calculate world pos based on normals and px,py
                val worldPip = when {
                    abs(face.normal[0]) > 0.9f -> floatArrayOf(face.normal[0] * h, px * h, py * h)
                    abs(face.normal[1]) > 0.9f -> floatArrayOf(px * h, face.normal[1] * h, py * h)
                    else -> floatArrayOf(px * h, py * h, face.normal[2] * h)
                }
                // Wait, the above logic is flawed because 'face.normal' is already rotated.
                // We need the pips to be relative to the rotated corners.
                // Vector interpolation: center + u*px + v*py
                
                val centerP = floatArrayOf(
                    face.corners.map { it[0] }.average().toFloat(),
                    face.corners.map { it[1] }.average().toFloat(),
                    face.corners.map { it[2] }.average().toFloat()
                )
                
                // Basis vectors for the face plane
                val vx = face.corners[1][0] - face.corners[0][0]
                val vy = face.corners[1][1] - face.corners[0][1]
                val vz = face.corners[1][2] - face.corners[0][2]
                
                val wx = face.corners[3][0] - face.corners[0][0]
                val wy = face.corners[3][1] - face.corners[0][1]
                val wz = face.corners[3][2] - face.corners[0][2]

                // Normalizing vectors might be overkill, rely on pips being -0.5..0.5
                // Corner 0 is -h, -h. Corner 1 is h, -h. Corner 3 is -h, h.
                // Center is 0,0.
                // pipPos = center + (vx/2h)*px*h*2 + (wx/2h)*py*h*2 ? No.
                // If corners are -h..h, then px scaled by 2h/2h?
                // pipPos = center + vx * (px/h) + wx * (py/h)
                
                val pip3D = floatArrayOf(
                    centerP[0] + (vx / size) * px * size + (wx / size) * py * size,
                    centerP[1] + (vy / size) * px * size + (wy / size) * py * size,
                    centerP[2] + (vz / size) * px * size + (wz / size) * py * size
                )
                
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

private data class RawFace(val normal: FloatArray, val corners: List<FloatArray>, val pips: List<Pair<Float, Float>>)
private data class ProcessedFace(val normal: FloatArray, val corners: List<FloatArray>, val pips: List<Pair<Float, Float>>, val avgZ: Float)

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
fun RizzPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue),
            contentAlignment = Alignment.Center
        ) {
            Rizz(rolling = false, onRollComplete = {})
        }
    }
}
