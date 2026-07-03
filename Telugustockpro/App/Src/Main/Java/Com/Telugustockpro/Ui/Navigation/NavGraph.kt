package com.telugustockpro.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.telugustockpro.ui.screens.analysis.AnalysisScreen
import com.telugustockpro.ui.screens.chart.ChartScreen
import com.telugustockpro.ui.screens.home.HomeScreen
import com.telugustockpro.ui.screens.markets.MarketsScreen
import com.telugustockpro.ui.theme.TradingViewColors

// ═══════════════════════════════════════════════════════════════
// SCREEN ROUTES
// ═══════════════════════════════════════════════════════════════

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Markets : Screen("markets")
    object Chart : Screen("chart/{stockSymbol}") {
        fun createRoute(stockSymbol: String) = "chart/$stockSymbol"
    }
    object Analysis : Screen("analysis/{stockSymbol}") {
        fun createRoute(stockSymbol: String) = "analysis/$stockSymbol"
    }
}

// ═══════════════════════════════════════════════════════════════
// BOTTOM NAVIGATION ITEM
// ═══════════════════════════════════════════════════════════════

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val selectedColor: Color,
    val badge: Int? = null
)

// ═══════════════════════════════════════════════════════════════
// MAIN APP COMPOSABLE
// ═══════════════════════════════════════════════════════════════

@Composable
fun TeluguStockMainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Bottom navigation items
    val bottomNavItems = listOf(
        BottomNavItem(
            screen = Screen.Home,
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            selectedColor = TradingViewColors.Blue,
            badge = null
        ),
        BottomNavItem(
            screen = Screen.Markets,
            label = "Markets",
            selectedIcon = Icons.Filled.ShowChart,
            unselectedIcon = Icons.Outlined.ShowChart,
            selectedColor = TradingViewColors.Green,
            badge = null
        )
    )

    // Check if current screen should show bottom bar
    val currentRoute = currentDestination?.route
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Markets.route
    )

    // Check for badge updates
    val hasAlerts = remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TradingViewColors.Background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                TeluguStockBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    hasAlerts = hasAlerts.value,
                    onItemClick = { item ->
                        navController.navigate(item.screen.route) {
                            // Pop up to the start destination to avoid
                            // building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
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
                .padding(paddingValues)
        ) {
            NavGraph(
                navController = navController,
                startDestination = Screen.Home.route
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// BOTTOM NAVIGATION BAR
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TeluguStockBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    hasAlerts: Boolean,
    onItemClick: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = TradingViewColors.NavBackground,
        contentColor = TradingViewColors.TextPrimary,
        tonalElevation = 0.dp,
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                shape = NavigationBarDefaults.shapes.container,
                ambientColor = Color.Black.copy(alpha = 0.3f)
            )
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route

            NavigationBarItem(
                icon = {
                    Box {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )

                        // Badge for alerts
                        if (item.label == "Home" && hasAlerts) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(TradingViewColors.Red)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = if (selected) 11.sp else 10.sp
                    )
                },
                selected = selected,
                onClick = { onItemClick(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = item.selectedColor,
                    selectedTextColor = item.selectedColor,
                    unselectedIconColor = TradingViewColors.NavUnselected,
                    unselectedTextColor = TradingViewColors.NavUnselected,
                    indicatorColor = item.selectedColor.copy(alpha = 0.12f)
                )
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// NAVIGATION GRAPH
// ═══════════════════════════════════════════════════════════════

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 300 },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -300 },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(250))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -300 },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 300 },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(250))
        }
    ) {
        // ─── HOME SCREEN ───
        composable(
            route = Screen.Home.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -100 },
                    animationSpec = tween(300)
                ) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -100 },
                    animationSpec = tween(300)
                ) + fadeOut(tween(300))
            }
        ) {
            HomeScreen(
                onStockClick = { symbol ->
                    navController.navigate(Screen.Chart.createRoute(symbol))
                },
                onSeeAllMarkets = {
                    navController.navigate(Screen.Markets.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // ─── MARKETS SCREEN ───
        composable(
            route = Screen.Markets.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 100 },
                    animationSpec = tween(300)
                ) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 100 },
                    animationSpec = tween(300)
                ) + fadeOut(tween(300))
            }
        ) {
            MarketsScreen(
                onStockClick = { symbol ->
                    navController.navigate(Screen.Chart.createRoute(symbol))
                }
            )
        }

        // ─── CHART SCREEN ───
        composable(
            route = Screen.Chart.route,
            arguments = listOf(
                navArgument("stockSymbol") {
                    type = NavType.StringType
                }
            ),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(250))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInVertically(
                    initialOffsetY = { -it / 3 },
                    animationSpec = tween(300)
                ) + fadeIn(tween(250))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { it / 3 },
                    animationSpec = tween(300)
                ) + fadeOut(tween(250))
            }
        ) { backStackEntry ->
            val stockSymbol = backStackEntry.arguments?.getString("stockSymbol") ?: "RELIANCE"
            ChartScreen(
                stockSymbol = stockSymbol,
                onBackClick = { navController.popBackStack() },
                onAnalysisClick = { symbol ->
                    navController.navigate(Screen.Analysis.createRoute(symbol))
                }
            )
        }

        // ─── ANALYSIS SCREEN ───
        composable(
            route = Screen.Analysis.route,
            arguments = listOf(
                navArgument("stockSymbol") {
                    type = NavType.StringType
                }
            ),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(250)) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(350)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(250))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(250)) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(350)
                )
            }
        ) { backStackEntry ->
            val stockSymbol = backStackEntry.arguments?.getString("stockSymbol") ?: "RELIANCE"
            AnalysisScreen(
                stockSymbol = stockSymbol,
                onBackClick = { navController.popBackStack() },
                onTradeClick = { symbol ->
                    // Navigate back to chart
                    navController.popBackStack(Screen.Chart.route, false)
                    navController.navigate(Screen.Chart.createRoute(symbol))
                }
            )
        }
    }
}
