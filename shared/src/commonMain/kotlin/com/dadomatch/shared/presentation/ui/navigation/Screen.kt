package com.dadomatch.shared.presentation.ui.navigation

import com.dadomatch.shared.presentation.ui.theme.AppConstants

sealed class Screen(val route: String) {
    data object Splash : Screen(AppConstants.Routes.SPLASH)
    data object Home : Screen(AppConstants.Routes.HOME)
    data object Successes : Screen(AppConstants.Routes.SUCCESSES)
    data object Profile : Screen(AppConstants.Routes.PROFILE)
    data object Settings : Screen(AppConstants.Routes.SETTINGS)
}

