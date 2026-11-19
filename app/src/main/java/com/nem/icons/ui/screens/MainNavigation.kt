package com.nem.icons.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.nem.icons.ui.MainViewModel

sealed class Screen(val route: String, val label: String,
                    val icon: ImageVector, val iconSelected: ImageVector) {
    object Apps : Screen("apps", "Apps",
        Icons.Outlined.Apps, Icons.Filled.Apps)
    object Library : Screen("library", "Library",
        Icons.Outlined.Collections, Icons.Filled.Collections)
    object Palettes : Screen("palettes", "Palettes",
        Icons.Outlined.Palette, Icons.Filled.Palette)
    object Settings : Screen("settings", "Settings",
        Icons.Outlined.Settings, Icons.Filled.Settings)
}

@Composable
fun MainNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Apps, Screen.Library, Screen.Palettes, Screen.Settings)

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDest = navBackStack?.destination
    val isEditorVisible = currentDest?.route == "editor"

    Scaffold(
        bottomBar = {
            if (!isEditorVisible) {
                NavigationBar {
                    screens.forEach { screen ->
                        val selected = currentDest?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) screen.iconSelected else screen.icon,
                                    contentDescription = screen.label
                                )
                            },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Apps.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it / 4 } + fadeOut() },
            popEnterTransition = { slideInHorizontally { -it / 4 } + fadeIn() },
            popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() }
        ) {
            composable(Screen.Apps.route) {
                AppsScreen(
                    viewModel = viewModel,
                    onOpenEditor = { navController.navigate("editor") }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(viewModel = viewModel)
            }
            composable(Screen.Palettes.route) {
                PalettesScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
            composable(
                route = "editor",
                enterTransition = { slideInVertically { it } + fadeIn() },
                exitTransition = { slideOutVertically { it } + fadeOut() }
            ) {
                EditorScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
