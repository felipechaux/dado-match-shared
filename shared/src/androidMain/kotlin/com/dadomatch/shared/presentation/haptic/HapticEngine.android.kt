package com.dadomatch.shared.presentation.haptic

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

actual class HapticEngine(private val view: View) {
    actual fun light() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    actual fun medium() {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    actual fun heavy() {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    actual fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
}

@Composable
actual fun rememberHapticEngine(): HapticEngine {
    val view = LocalView.current
    return remember { HapticEngine(view) }
}
