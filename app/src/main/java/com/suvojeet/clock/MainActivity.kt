package com.suvojeet.clock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.suvojeet.clock.ui.navigation.Screen
import com.suvojeet.clock.ui.theme.CosmicTheme
import com.suvojeet.clock.ui.clock.ClockScreen
import com.suvojeet.clock.ui.alarm.AlarmScreen
import com.suvojeet.clock.ui.timer.TimerScreen
import com.suvojeet.clock.ui.stopwatch.StopwatchScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CosmicTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Schedule, contentDescription = "Clock") },
                    label = { Text("Clock") },
                    selected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Clock>() } == true,
                    onClick = {
                        navController.navigate(Screen.Clock) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AccessAlarm, contentDescription = "Alarm") },
                    label = { Text("Alarm") },
                    selected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Alarm>() } == true,
                    onClick = {
                        navController.navigate(Screen.Alarm) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.HourglassEmpty, contentDescription = "Timer") },
                    label = { Text("Timer") },
                    selected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Timer>() } == true,
                    onClick = {
                        navController.navigate(Screen.Timer) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Timer, contentDescription = "Stopwatch") },
                    label = { Text("Stopwatch") },
                    selected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Stopwatch>() } == true,
                    onClick = {
                        navController.navigate(Screen.Stopwatch) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Clock,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Clock> { ClockScreen() }
            composable<Screen.Alarm> { AlarmScreen() }
            composable<Screen.Timer> { TimerScreen() }
            composable<Screen.Stopwatch> { StopwatchScreen() }
        }
    }
}