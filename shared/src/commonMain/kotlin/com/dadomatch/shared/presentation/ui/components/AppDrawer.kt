package com.dadomatch.shared.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dadomatch.shared.presentation.ui.navigation.Screen

@Composable
fun AppDrawer(
    currentRoute: String?,
    navigateTo: (String) -> Unit,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        DrawerHeader()
        Spacer(modifier = Modifier.height(12.dp))
        
        val items = listOf(
            DrawerItem(Screen.Home, Icons.Default.Home),
            DrawerItem(Screen.Profile, Icons.Default.Person),
            DrawerItem(Screen.Settings, Icons.Default.Settings)
        )

        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.screen.title) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    navigateTo(item.screen.route)
                    closeDrawer()
                },
                icon = { Icon(item.icon, contentDescription = item.screen.title) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

@Composable
private fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = "Dado Match",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private data class DrawerItem(val screen: Screen, val icon: ImageVector)
