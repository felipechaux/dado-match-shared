package com.dadomatch.shared.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = NeonPink,
    secondary = NeonCyan,
    tertiary = NeonPurple,
    background = DeepDarkBlue,
    surface = DarkSurface,
    onPrimary = TextWhite,
    onSecondary = TextWhite,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite
)

// Material defaults with the small styles bumped so secondary text stays
// readable: nothing a user has to read goes below 14sp
private val BaseTypography = Typography()
private val AppTypography = BaseTypography.copy(
    bodyLarge = BaseTypography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = BaseTypography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = BaseTypography.bodySmall.copy(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = BaseTypography.labelLarge.copy(fontSize = 15.sp, lineHeight = 21.sp),
    labelMedium = BaseTypography.labelMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = BaseTypography.labelSmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
