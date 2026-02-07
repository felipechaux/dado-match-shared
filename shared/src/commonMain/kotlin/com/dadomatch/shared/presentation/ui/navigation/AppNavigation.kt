package com.dadomatch.shared.presentation.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.dadomatch.shared.presentation.ui.components.LiquidFooterMenu
import com.dadomatch.shared.presentation.ui.screens.HomeScreen
import com.dadomatch.shared.presentation.ui.screens.PaywallScreen
import com.dadomatch.shared.presentation.ui.screens.ProfileScreen
import com.dadomatch.shared.presentation.ui.screens.SettingsScreen
import com.dadomatch.shared.presentation.ui.screens.SplashScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != Screen.Splash.route && currentRoute != Screen.Paywall.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                LiquidFooterMenu(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().route ?: Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showBottomBar) paddingValues.calculateBottomPadding() else 0.dp)
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToPaywall = {
                            navController.navigate(Screen.Paywall.route)
                        }
                    )
                }
                composable(Screen.Successes.route) {
                    com.dadomatch.shared.presentation.ui.screens.SuccessesScreen()
                }
                composable(Screen.Profile.route) {
                    ProfileScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateToPaywall = {
                            navController.navigate(Screen.Paywall.route)
                        }
                    )
                }
                composable(Screen.Paywall.route) {
                    PaywallScreen(
                        onDismiss = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AppNavigationPreview() {
    AppNavigation()
}
