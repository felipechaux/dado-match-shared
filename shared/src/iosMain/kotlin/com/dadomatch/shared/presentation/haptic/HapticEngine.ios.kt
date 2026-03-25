package com.dadomatch.shared.presentation.haptic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy
import platform.UIKit.UIImpactFeedbackStyle.UIImpactFeedbackStyleLight
import platform.UIKit.UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType.UINotificationFeedbackTypeSuccess

actual class HapticEngine {
    actual fun light() {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyleLight).impactOccurred()
    }

    actual fun medium() {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyleMedium).impactOccurred()
    }

    actual fun heavy() {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyleHeavy).impactOccurred()
    }

    actual fun success() {
        UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackTypeSuccess)
    }
}

@Composable
actual fun rememberHapticEngine(): HapticEngine = remember { HapticEngine() }
