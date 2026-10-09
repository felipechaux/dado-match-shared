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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dadomatch.shared.feature.game.presentation.GameScreen
import com.dadomatch.shared.feature.icebreaker.presentation.ui.HomeScreen
import com.dadomatch.shared.feature.icebreaker.presentation.viewmodel.HomeViewModel
import com.dadomatch.shared.feature.subscription.domain.usecase.GetLanguageUseCase
import com.dadomatch.shared.feature.subscription.presentation.ui.PaywallScreen
import com.dadomatch.shared.feature.subscription.presentation.ui.ProfileScreen
import com.dadomatch.shared.feature.subscription.presentation.ui.SettingsScreen
import com.dadomatch.shared.presentation.ui.LocaleProvider
import com.dadomatch.shared.presentation.ui.components.LiquidFooterMenu
import com.dadomatch.shared.presentation.ui.screens.SplashScreen
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.dadomatch.shared.shared.generated.resources.Res
import com.dadomatch.shared.shared.generated.resources.nav_home
import com.dadomatch.shared.shared.generated.resources.nav_profile
import com.dadomatch.shared.shared.generated.resources.nav_settings
import com.dadomatch.shared.shared.generated.resources.nav_success
import com.dadomatch.shared.presentation.ui.components.glassSource
import com.dadomatch.shared.presentation.ui.components.rememberGlassSource
import org.koin.compose.koinInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.dadomatch.shared.feature.engagement.domain.EngagementManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

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
    navController: NavHostController = rememberNavController(),
    // When set, a platform-native tab bar (iOS) replaces LiquidFooterMenu
    nativeTabBar: NativeTabBarBridge? = null
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
    // Screens are what the glass bottom bar floating above them refracts
    val glassSource = rememberGlassSource()

    // Re-plan reminders whenever the app is in front again. ON_RESUME rather than
    // ON_START so the answer to the notification permission dialog is picked up too.
    val engagementManager: EngagementManager = koinInject()
    val engagementScope = rememberCoroutineScope()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        engagementScope.launch {
            try {
                engagementManager.onAppForeground()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                // Reminders are best effort; the next resume tries again
            }
        }
    }

    val homeViewModel: HomeViewModel = koinViewModel()
    val homeUiState by homeViewModel.uiState.collectAsState()

    val showBottomBar = currentRoute != Screen.Splash.route &&
                        currentRoute != Screen.Paywall.route &&
                        currentRoute != Screen.Game.route &&
                        !homeUiState.showOnboarding

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            // Home is the root of the tab stack: the graph's start destination is
            // Splash, which is already popped, so popping up to it would be a no-op
            // and every tab switch would pile up on the back stack
            popUpTo(Screen.Home.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState    = true
        }
    }

    if (nativeTabBar != null) {
        DisposableEffect(nativeTabBar) {
            nativeTabBar.onTabSelected = { index -> TAB_ORDER.getOrNull(index)?.let(navigateToTab) }
            onDispose { nativeTabBar.onTabSelected = null }
        }
        LaunchedEffect(nativeTabBar, currentRoute, showBottomBar) {
            nativeTabBar.publishState(
                selectedIndex = tabIndex(currentRoute).takeIf { it >= 0 } ?: NativeTabBarBridge.NO_TAB_SELECTED,
                isVisible = showBottomBar
            )
        }
    }

    LocaleProvider(languageCode = selectedLanguage) {
    if (nativeTabBar != null) {
        // Resolved inside LocaleProvider so the native bar follows the in-app language
        val tabTitles = listOf(
            stringResource(Res.string.nav_home),
            stringResource(Res.string.nav_success),
            stringResource(Res.string.nav_profile),
            stringResource(Res.string.nav_settings)
        )
        LaunchedEffect(nativeTabBar, tabTitles) { nativeTabBar.publishTitles(tabTitles) }
    }
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
                        .padding(top = paddingValues.calculateTopPadding())
                        .glassSource(glassSource),
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
                            onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) },
                            onNavigateToGame = { navController.navigate(Screen.Game.route) }
                        )
                    }
                    composable(Screen.Game.route) {
                        GameScreen(
                            onBack = { navController.popBackStack() },
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
                    visible = showBottomBar && nativeTabBar == null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit  = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    LiquidFooterMenu(
                        currentRoute = currentRoute,
                        glassSource  = glassSource,
                        onNavigate   = navigateToTab
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
