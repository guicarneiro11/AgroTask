package com.guicarneirodev.agrotask.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.guicarneirodev.agrotask.presentation.screens.ActivityScreen
import com.guicarneirodev.agrotask.presentation.screens.TaskScreen
import com.guicarneirodev.agrotask.presentation.screens.WeatherScreen

@Composable
fun AgroTaskNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Tasks.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Tasks.route) {
                TaskScreen()
            }
            composable(Screen.Activity.route) {
                ActivityScreen()
            }
            composable(Screen.Weather.route) {
                WeatherScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    val items = listOf(
        Screen.Tasks,
        Screen.Activity,
        Screen.Weather
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Tasks.route) {
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

sealed class Screen(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Tasks : Screen("tasks", "Tarefas", Icons.Default.Home)
    object Activity : Screen("activity", "Atividades", Icons.Default.Add)
    object Weather : Screen("weather", "Clima", Icons.Default.Cloud)
}