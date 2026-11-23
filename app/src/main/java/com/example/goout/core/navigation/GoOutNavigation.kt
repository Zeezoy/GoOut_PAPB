package com.example.goout.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.goout.core.designsystem.GoOutColors
import com.example.goout.feature.auth.LoginScreen
import com.example.goout.feature.auth.RegisterScreen
import com.example.goout.feature.auth.SplashScreen
import com.example.goout.feature.corridor.CorridorDetailScreen
import com.example.goout.feature.corridor.CorridorListScreen
import com.example.goout.feature.home.HomeScreen
import com.example.goout.feature.profile.ProfileScreen
import com.example.goout.feature.report.ReportScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAP = "map"            // Peta
    const val CORRIDORS = "corridors" // Koridor
    const val REPORT = "report"      // Lapor
    const val PROFILE = "profile"
    const val CORRIDOR_DETAIL = "corridor/{corridorId}"
    fun corridorDetail(id: String) = "corridor/$id"
}

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.MAP, "Peta", Icons.Filled.Map),
    BottomTab(Routes.CORRIDORS, "Koridor", Icons.Filled.AltRoute),
    BottomTab(Routes.REPORT, "Lapor", Icons.Filled.AddLocationAlt),
)

@Composable
fun GoOutApp() {
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    Scaffold(
        containerColor = GoOutColors.Background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = GoOutColors.Background) {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoOutColors.Primary,
                                selectedTextColor = GoOutColors.Primary,
                                indicatorColor = GoOutColors.PrimaryContainer,
                                unselectedIconColor = GoOutColors.TextSecondary,
                                unselectedTextColor = GoOutColors.TextSecondary,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onContinue = {
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onLogin = { navController.navigate(Routes.LOGIN) },
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onSuccess = {
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onRegister = { navController.navigate(Routes.REGISTER) },
                    onGuest = {
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onSuccess = {
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.MAP) {
                HomeScreen(
                    onCorridorClick = { navController.navigate(Routes.corridorDetail(it)) },
                    onReportClick = { navController.navigate(Routes.REPORT) },
                    onProfileClick = { navController.navigate(Routes.PROFILE) },
                )
            }
            composable(Routes.CORRIDORS) {
                CorridorListScreen(
                    onCorridorClick = { navController.navigate(Routes.corridorDetail(it)) },
                )
            }
            composable(Routes.CORRIDOR_DETAIL) {
                CorridorDetailScreen(
                    onBack = { navController.popBackStack() },
                    onReport = { navController.navigate(Routes.REPORT) },
                )
            }
            composable(Routes.REPORT) {
                ReportScreen(
                    onBackToMap = {
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.MAP) { inclusive = true }
                        }
                    },
                    onLogin = { navController.navigate(Routes.LOGIN) },
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogin = { navController.navigate(Routes.LOGIN) },
                )
            }
        }
    }
}
