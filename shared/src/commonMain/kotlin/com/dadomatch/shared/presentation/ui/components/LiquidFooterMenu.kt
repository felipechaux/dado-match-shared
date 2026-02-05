package com.dadomatch.shared.presentation.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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

@Composable
fun LiquidFooterMenu(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val homeRoute = Screen.Home.route
    val successesRoute = Screen.Successes.route
    val profileRoute = Screen.Profile.route
    val settingsRoute = Screen.Settings.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .height(72.dp)
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(36.dp)
            ),
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
                onClick = { onNavigate(homeRoute) }
            )
            FooterItem(
                icon = Icons.Default.Star,
                label = stringResource(Res.string.nav_success),
                isSelected = currentRoute == successesRoute,
                onClick = { onNavigate(successesRoute) }
            )
            FooterItem(
                icon = Icons.Default.Person,
                label = stringResource(Res.string.nav_profile),
                isSelected = currentRoute == profileRoute,
                onClick = { onNavigate(profileRoute) }
            )
            FooterItem(
                icon = Icons.Default.Settings,
                label = stringResource(Res.string.nav_settings),
                isSelected = currentRoute == settingsRoute,
                onClick = { onNavigate(settingsRoute) }
            )
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
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
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
