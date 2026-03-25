package com.dadomatch.shared.presentation.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dadomatch.shared.feature.icebreaker.presentation.ui.HomeScreen
import com.dadomatch.shared.feature.subscription.domain.usecase.GetLanguageUseCase
import com.dadomatch.shared.feature.subscription.presentation.ui.PaywallScreen
import com.dadomatch.shared.feature.subscription.presentation.ui.ProfileScreen
import com.dadomatch.shared.feature.subscription.presentation.ui.SettingsScreen
import com.dadomatch.shared.presentation.ui.LocaleProvider
import com.dadomatch.shared.presentation.ui.components.LiquidFooterMenu
import com.dadomatch.shared.presentation.ui.screens.SplashScreen
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

// Tab order determines slide direction: higher index → slide from right, lower → from left
private val TAB_ORDER = listOf(
    Screen.Home.route,
    Screen.Successes.route,
    Screen.Profile.route,
    Screen.Settings.route
)

private fun tabIndex(route: String?) = TAB_ORDER.indexOf(route)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Read system locale before LocaleProvider overrides LocalConfiguration.
    val deviceLanguage = Locale.current.language.take(2)
    val getLanguageUseCase: GetLanguageUseCase = koinInject()
    // Stabilise the flow reference so collectAsState doesn't restart on every recomposition.
    val languageFlow = remember(getLanguageUseCase) { getLanguageUseCase(deviceLanguage) }
    val selectedLanguage by languageFlow.collectAsState(initial = deviceLanguage)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showConfettiOnSettings by remember { mutableStateOf(false) }

    val showBottomBar = currentRoute != Screen.Splash.route && currentRoute != Screen.Paywall.route

    LocaleProvider(languageCode = selectedLanguage) {
    Scaffold(containerColor = DeepDarkBlue) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DeepDarkBlue
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding()),
                    enterTransition = {
                        val from = tabIndex(initialState.destination.route)
                        val to   = tabIndex(targetState.destination.route)
                        val dir  = if (from == -1 || to == -1 || to >= from) 1 else -1
                        slideInHorizontally(
                            initialOffsetX = { (it * 0.35f * dir).toInt() },
                            animationSpec  = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300))
                    },
                    exitTransition = {
                        val from = tabIndex(initialState.destination.route)
                        val to   = tabIndex(targetState.destination.route)
                        val dir  = if (from == -1 || to == -1 || to >= from) -1 else 1
                        slideOutHorizontally(
                            targetOffsetX = { (it * 0.35f * dir).toInt() },
                            animationSpec  = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(200))
                    },
                    popEnterTransition = {
                        val from = tabIndex(initialState.destination.route)
                        val to   = tabIndex(targetState.destination.route)
                        val dir  = if (from == -1 || to == -1 || to <= from) -1 else 1
                        slideInHorizontally(
                            initialOffsetX = { (it * 0.35f * dir).toInt() },
                            animationSpec  = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300))
                    },
                    popExitTransition = {
                        val from = tabIndex(initialState.destination.route)
                        val to   = tabIndex(targetState.destination.route)
                        val dir  = if (from == -1 || to == -1 || to <= from) 1 else -1
                        slideOutHorizontally(
                            targetOffsetX = { (it * 0.35f * dir).toInt() },
                            animationSpec  = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(200))
                    }
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
                            onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) }
                        )
                    }
                    composable(Screen.Successes.route) {
                        com.dadomatch.shared.feature.success.presentation.ui.SuccessesScreen(
                            onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) }
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen()
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) },
                            showConfettiOnEnter = showConfettiOnSettings,
                            onConfettiConsumed = { showConfettiOnSettings = false }
                        )
                    }
                    composable(
                        route = Screen.Paywall.route,
                        enterTransition = {
                            slideInVertically(
                                initialOffsetY = { it },
                                animationSpec  = tween(380, easing = FastOutSlowInEasing)
                            ) + fadeIn(tween(300))
                        },
                        exitTransition = {
                            slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec  = tween(320, easing = FastOutSlowInEasing)
                            ) + fadeOut(tween(250))
                        },
                        popExitTransition = {
                            slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec  = tween(320, easing = FastOutSlowInEasing)
                            ) + fadeOut(tween(250))
                        }
                    ) {
                        PaywallScreen(
                            onDismiss = {
                                if (showConfettiOnSettings) {
                                    navController.navigate(Screen.Settings.route) {
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.popBackStack()
                                }
                            },
                            onPurchaseSuccess = { showConfettiOnSettings = true }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit  = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    LiquidFooterMenu(
                        currentRoute = currentRoute,
                        onNavigate   = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().route ?: Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        }
    }
    } // LocaleProvider
}

@Preview
@Composable
fun AppNavigationPreview() {
    AppNavigation()
}
