package com.dadomatch.shared.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dadomatch.shared.presentation.haptic.rememberHapticEngine
import com.dadomatch.shared.presentation.ui.navigation.Screen
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.NeonPink
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.nav_home
import com.dadomatch.shared.shared.generated.resources.nav_profile
import com.dadomatch.shared.shared.generated.resources.nav_settings
import com.dadomatch.shared.shared.generated.resources.nav_success
import org.jetbrains.compose.resources.stringResource

private val BarShape = RoundedCornerShape(36.dp)
private val BarHeight = 72.dp
private val BarBottomMargin = 12.dp

/**
 * Bottom space a screen must leave so its last item isn't hidden behind the
 * floating bar: the bar itself plus a small gap, on top of the system
 * navigation bar inset. Also clears the native iOS tab bar, which is shorter.
 */
fun Modifier.bottomBarClearance(): Modifier =
    navigationBarsPadding().padding(bottom = BarHeight + BarBottomMargin + 8.dp)

@Composable
fun LiquidFooterMenu(
    currentRoute: String?,
    glassSource: GlassSource,
    onNavigate: (String) -> Unit
) {
    val homeRoute = Screen.Home.route
    val successesRoute = Screen.Successes.route
    val profileRoute = Screen.Profile.route
    val settingsRoute = Screen.Settings.route
    val haptic = rememberHapticEngine()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Sit above the system navigation bar (gesture pill or 3-button bar)
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = BarBottomMargin),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .glassSurface(glassSource, BarShape),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FooterItem(
                    icon = Icons.Default.Home,
                    label = stringResource(Res.string.nav_home),
                    isSelected = currentRoute == homeRoute,
                    onClick = { haptic.light(); onNavigate(homeRoute) }
                )
                FooterItem(
                    icon = Icons.Default.Star,
                    label = stringResource(Res.string.nav_success),
                    isSelected = currentRoute == successesRoute,
                    onClick = { haptic.light(); onNavigate(successesRoute) }
                )
                FooterItem(
                    icon = Icons.Default.Person,
                    label = stringResource(Res.string.nav_profile),
                    isSelected = currentRoute == profileRoute,
                    onClick = { haptic.light(); onNavigate(profileRoute) }
                )
                FooterItem(
                    icon = Icons.Default.Settings,
                    label = stringResource(Res.string.nav_settings),
                    isSelected = currentRoute == settingsRoute,
                    onClick = { haptic.light(); onNavigate(settingsRoute) }
                )
            }
        }
    }
}

@Composable
private fun FooterItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) NeonCyan else TextWhite.copy(alpha = 0.6f)
    // Selected tab sits on a cyan-tinted glass capsule, like the iOS 26 tab bar
    val capsuleColor by animateColorAsState(
        if (isSelected) NeonCyan.copy(alpha = 0.14f) else Color.Transparent
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 10.dp)
            .widthIn(min = 64.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(capsuleColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .background(NeonPink, RoundedCornerShape(2.dp))
            )
        }
    }
}
