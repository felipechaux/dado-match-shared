package com.dadomatch.shared.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import kotlinx.coroutines.isActive
import kotlin.random.Random

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    val size: Float,
    val color: Color,
    val speedX: Float,
    val speedY: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    var opacity: Float = 1f
)

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    durationMillis: Long = 4000L,
    onAnimationEnd: () -> Unit = {}
) {
    val particles = remember { mutableListOf<ConfettiParticle>() }
    var triggerDraw by remember { mutableStateOf(0) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val colors = listOf(
        NeonPink, 
        NeonCyan, 
        Color(0xFFFFD700), // Gold
        Color(0xFF00FF00), // Green
        Color(0xFF8A2BE2), // Violet
        Color(0xFFFF4500)  // Orange Red
    )

    LaunchedEffect(Unit) {
        // Wait for canvas to be sized
        while (canvasSize == Size.Zero && isActive) {
            withFrameMillis { }
        }
        
        if (!isActive) return@LaunchedEffect
        
        val startTime = withFrameMillis { it }
        
        // Initial burst
        repeat(200) {
            particles.add(createParticle(colors, canvasSize.width))
        }

        while (isActive) {
            withFrameMillis { frameTime ->
                val elapsed = frameTime - startTime
                
                if (elapsed > durationMillis && particles.isEmpty()) {
                    onAnimationEnd()
                    return@withFrameMillis
                }

                // Update particles
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    
                    val nextY = p.y + p.speedY
                    val nextX = p.x + p.speedX
                    val nextOpacity = if (elapsed > durationMillis * 0.7) {
                        (p.opacity - 0.01f).coerceAtLeast(0f)
                    } else p.opacity

                    if (nextY > canvasSize.height + 50f || nextOpacity <= 0f) {
                        iterator.remove()
                    } else {
                        p.x = nextX
                        p.y = nextY
                        p.rotation += p.rotationSpeed
                        p.opacity = nextOpacity
                    }
                }
                
                // Add continuous flow during the first part
                if (elapsed < durationMillis * 0.6 && particles.size < 300) {
                    repeat(5) { particles.add(createParticle(colors, canvasSize.width)) }
                }
                
                triggerDraw++
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val currentDraw = triggerDraw // Read state to force redraw
        if (canvasSize != size) {
            canvasSize = size
        }
        
        particles.forEach { p ->
            rotate(p.rotation, Offset(p.x + p.size / 2, p.y + p.size / 2)) {
                drawRect(
                    color = p.color.copy(alpha = p.opacity),
                    topLeft = Offset(p.x, p.y),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}

private fun createParticle(colors: List<Color>, maxWidth: Float): ConfettiParticle {
    return ConfettiParticle(
        x = Random.nextFloat() * maxWidth,
        y = -Random.nextFloat() * 200f - 50f, // Vary start height
        size = Random.nextFloat() * 15f + 15f,
        color = colors.random(),
        speedX = Random.nextFloat() * 6f - 3f,
        speedY = Random.nextFloat() * 12f + 8f,
        rotation = Random.nextFloat() * 360f,
        rotationSpeed = Random.nextFloat() * 20f - 10f
    )
}
