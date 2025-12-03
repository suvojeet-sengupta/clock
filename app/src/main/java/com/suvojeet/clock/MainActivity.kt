package com.suvojeet.clock

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkPermissions()
        handleSetAlarmIntent(intent)

        setContent {
            CosmicTheme {
                MainScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSetAlarmIntent(intent)
    }

    private fun handleSetAlarmIntent(intent: Intent) {
        if (intent.action == AlarmClock.ACTION_SET_ALARM) {
            val hour = intent.getIntExtra(AlarmClock.EXTRA_HOUR, -1)
            val minutes = intent.getIntExtra(AlarmClock.EXTRA_MINUTES, -1)
            val message = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE) ?: "Alarm"
            val skipUi = intent.getBooleanExtra(AlarmClock.EXTRA_SKIP_UI, false)

            if (hour != -1 && minutes != -1) {
                val application = applicationContext as ClockApplication
                val repository = com.suvojeet.clock.data.alarm.AlarmRepository(application.database.alarmDao())
                val scheduler = com.suvojeet.clock.data.alarm.AndroidAlarmScheduler(this)
                
                CoroutineScope(Dispatchers.IO).launch {
                    val time = LocalTime.of(hour, minutes)
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    val alarm = com.suvojeet.clock.data.alarm.AlarmEntity(
                        time = time.format(formatter),
                        label = message,
                        isEnabled = true
                    )
                    val newAlarmId = repository.insert(alarm)
                    
                    val scheduledAlarm = alarm.copy(id = newAlarmId.toInt())
                    scheduler.schedule(scheduledAlarm)
                    
                    if (!skipUi) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(this@MainActivity, "Alarm set for ${time.format(formatter)}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(this@MainActivity, "Alarm set for ${time.format(formatter)} (UI skipped)", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Could not set alarm: Invalid time", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 101)
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(android.app.AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!android.provider.Settings.canDrawOverlays(this)) {
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var showMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Show TopAppBar only on main screens, not on Settings (Settings has its own)
            val isMainScreen = currentDestination?.hierarchy?.any { 
                it.hasRoute<Screen.Clock>() || it.hasRoute<Screen.WorldClock>() || it.hasRoute<Screen.Alarm>() || 
                it.hasRoute<Screen.Timer>() || it.hasRoute<Screen.Stopwatch>()
            } == true

            if (isMainScreen) {
                val title = when {
                    currentDestination?.hasRoute<Screen.Clock>() == true -> "Clock"
                    currentDestination?.hasRoute<Screen.WorldClock>() == true -> "World Clock"
                    currentDestination?.hasRoute<Screen.Alarm>() == true -> "Alarm"
                    currentDestination?.hasRoute<Screen.Timer>() == true -> "Timer"
                    currentDestination?.hasRoute<Screen.Stopwatch>() == true -> "Stopwatch"
                    else -> "Clock"
                }
                TopAppBar(
                    title = { Text(title) },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Screen.Settings)
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Hide BottomBar on Settings screen
            val isSettings = currentDestination?.hasRoute<Screen.Settings>() == true
            
            if (!isSettings) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    val items = listOf(
                        Triple(Screen.Clock, Icons.Filled.Schedule, "Clock"),
                        Triple(Screen.WorldClock, Icons.Filled.Public, "World"),
                        Triple(Screen.Alarm, Icons.Filled.AccessAlarm, "Alarm"),
                        Triple(Screen.Timer, Icons.Filled.HourglassEmpty, "Timer"),
                        Triple(Screen.Stopwatch, Icons.Filled.Timer, "Stopwatch")
                    )

                    items.forEach { (screen, icon, label) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { 
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                ) 
                            },
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(screen::class) } == true,
                            onClick = {
                                navController.navigate(screen) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Clock,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Clock> { ClockScreen() }
            composable<Screen.WorldClock> { com.suvojeet.clock.ui.clock.WorldClockScreen() }
            composable<Screen.Alarm> { AlarmScreen() }
            composable<Screen.Timer> { TimerScreen() }
            composable<Screen.Stopwatch> { StopwatchScreen() }
            composable<Screen.Settings> { 
                com.suvojeet.clock.ui.settings.SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                ) 
            }
        }
    }
}