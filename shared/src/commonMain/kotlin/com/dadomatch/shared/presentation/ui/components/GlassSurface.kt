package com.dadomatch.shared.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/** Content that a [glassSurface] samples and refracts. */
expect class GlassSource

@Composable
expect fun rememberGlassSource(): GlassSource

/** Records this content so [glassSurface]s drawn above it can show it through the glass. */
expect fun Modifier.glassSource(source: GlassSource): Modifier

/** Draws a "liquid glass" surface in [shape] over the content recorded by [source]. */
expect fun Modifier.glassSurface(source: GlassSource, shape: Shape): Modifier
