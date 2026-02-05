package com.dadomatch.shared.presentation.ui.theme

import androidx.compose.ui.graphics.Color
import com.dadomatch.shared.presentation.ui.theme.*

object AppConstants {
    val ENVIRONMENTS = listOf("env_gym", "env_party", "env_library", "env_cafe")
    val INTENSITIES = listOf("int_cringe", "int_romantic", "int_direct", "int_funny")
    
    fun getEnvironmentColor(key: String): Color = when(key) {
        "env_gym" -> NeonOrange
        "env_party" -> NeonPurple
        "env_library" -> NeonCyan
        "env_cafe" -> NeonAmber
        else -> NeonPink
    }

    fun getIntensityColor(key: String): Color = when(key) {
        "int_cringe" -> NeonRed
        "int_romantic" -> NeonPink
        "int_direct" -> NeonMint
        "int_funny" -> NeonYellow
        else -> NeonPink
    }

    object Routes {

        const val SPLASH = "splash"
        const val HOME = "home"
        const val SUCCESSES = "successes"
        const val PROFILE = "profile"
        const val SETTINGS = "settings"
    }
}
