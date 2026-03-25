package com.dadomatch.shared.presentation.haptic

import androidx.compose.runtime.Composable

expect class HapticEngine {
    fun light()
    fun medium()
    fun heavy()
    fun success()
}

@Composable
expect fun rememberHapticEngine(): HapticEngine
