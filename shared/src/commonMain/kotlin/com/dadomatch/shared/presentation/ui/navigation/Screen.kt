package com.dadomatch.shared.presentation.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Home")
    data object Profile : Screen("profile", "Profile")
    data object Settings : Screen("settings", "Settings")
}
