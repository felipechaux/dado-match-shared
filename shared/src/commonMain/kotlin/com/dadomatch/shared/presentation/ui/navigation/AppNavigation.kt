package com.dadomatch.shared.presentation.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dadomatch.shared.feature.icebreaker.presentation.ui.HomeScreen
import com.dadomatch.shared.feature.subscription.presentation.ui.PaywallScreen
import com.dadomatch.shared.feature.subscription.presentation.ui.ProfileScreen
import com.dadomatch.shared.feature.subscription.presentation.ui.SettingsScreen
import com.dadomatch.shared.presentation.ui.components.LiquidFooterMenu
import com.dadomatch.shared.presentation.ui.screens.SplashScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showConfettiOnSettings by remember { mutableStateOf(false) }

    val showBottomBar = currentRoute != Screen.Splash.route && currentRoute != Screen.Paywall.route

    Scaffold(
        containerColor = com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue,
    ) { paddingValues ->
        // Use a Surface with the background color to prevent black flickering during screen transitions
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
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
                        com.dadomatch.shared.feature.success.presentation.ui.SuccessesScreen(
                            onNavigateToPaywall = {
                                navController.navigate(Screen.Paywall.route)
                            }
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen()
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onNavigateToPaywall = {
                                navController.navigate(Screen.Paywall.route)
                            },
                            showConfettiOnEnter = showConfettiOnSettings,
                            onConfettiConsumed = { showConfettiOnSettings = false }
                        )
                    }
                    composable(Screen.Paywall.route) {
                        PaywallScreen(
                            onDismiss = {
                                if (showConfettiOnSettings) {
                                    // Purchase happened — RC already dismissed the Paywall,
                                    // so just navigate to Settings (no extra popBackStack).
                                    navController.navigate(Screen.Settings.route) {
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.popBackStack()
                                }
                            },
                            onPurchaseSuccess = {
                                showConfettiOnSettings = true
                            }
                        )
                    }
                }

                // Floating Menu Overlay
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
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
        }
    }
}

@Preview
@Composable
fun AppNavigationPreview() {
    AppNavigation()
}
