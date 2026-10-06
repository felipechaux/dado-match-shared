package com.dadomatch.shared.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue

// iOS renders a native UITabBar with system Liquid Glass (see NativeTabBarBridge),
// so this only backs the Compose bar on the legacy MainViewController() entry point.
actual class GlassSource

@Composable
actual fun rememberGlassSource(): GlassSource = remember { GlassSource() }

actual fun Modifier.glassSource(source: GlassSource): Modifier = this

actual fun Modifier.glassSurface(source: GlassSource, shape: Shape): Modifier =
    background(DeepDarkBlue.copy(alpha = 0.9f), shape)
