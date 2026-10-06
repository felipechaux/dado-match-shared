package com.dadomatch.shared.presentation.ui.components

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

actual class GlassSource(internal val backdrop: LayerBackdrop)

@Composable
actual fun rememberGlassSource(): GlassSource {
    val backdrop = rememberLayerBackdrop()
    return remember(backdrop) { GlassSource(backdrop) }
}

actual fun Modifier.glassSource(source: GlassSource): Modifier = layerBackdrop(source.backdrop)

// Blur needs Android 12 and the lens (refraction) shader Android 13; below
// that the effects are skipped, so the surface tint goes near-opaque to keep
// the content on top legible.
private val SurfaceTint =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Color.Black.copy(alpha = 0.25f)
    else DeepDarkBlue.copy(alpha = 0.9f)

actual fun Modifier.glassSurface(source: GlassSource, shape: Shape): Modifier = drawBackdrop(
    backdrop = source.backdrop,
    shape = { shape },
    effects = {
        vibrancy()
        blur(2.dp.toPx())
        lens(refractionHeight = 16.dp.toPx(), refractionAmount = 32.dp.toPx())
    },
    onDrawSurface = { drawRect(SurfaceTint) }
)
