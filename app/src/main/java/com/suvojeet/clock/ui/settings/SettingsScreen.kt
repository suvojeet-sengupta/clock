package com.suvojeet.clock.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.suvojeet.clock.data.settings.AppTheme
import com.suvojeet.clock.data.settings.DismissMethod
import com.suvojeet.clock.data.settings.MathDifficulty
import com.suvojeet.clock.util.HapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLinkAlexaClick: () -> Unit,
    onSleepTimerClick: () -> Unit = {}
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val view = LocalView.current
    val context = LocalContext.current
    
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val hapticFeedbackEnabled by viewModel.hapticFeedbackEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Section: Display
            SettingsSectionHeader(title = "Display")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "24-Hour Format",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Use 24-hour format instead of 12-hour",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = is24HourFormat,
                    onCheckedChange = { 
                        if (hapticFeedbackEnabled) HapticFeedback.performToggle(view)
                        viewModel.set24HourFormat(it) 
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            
            // Theme Selection
            val appTheme by viewModel.appTheme.collectAsState()
            var showThemeDialog by remember { mutableStateOf(false) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                        showThemeDialog = true 
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = appTheme.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (showThemeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeDialog = false },
                    title = { Text("Select Theme") },
                    text = {
                        Column {
                            AppTheme.values().forEach { theme ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                                            viewModel.setAppTheme(theme)
                                            showThemeDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (theme == appTheme),
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = theme.displayName,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = theme.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemeDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Sleep Timer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                        onSleepTimerClick() 
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Bedtime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Sleep Timer",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Fall asleep with soothing sounds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Section: Alarm Settings
            SettingsSectionHeader(title = "Alarm")

            // Gradual Volume
            val gradualVolume by viewModel.gradualVolume.collectAsState()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Gradual Volume",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Increase volume over 30 seconds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = gradualVolume,
                    onCheckedChange = { 
                        if (hapticFeedbackEnabled) HapticFeedback.performToggle(view)
                        viewModel.setGradualVolume(it) 
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Dismiss Method
            val dismissMethod by viewModel.dismissMethod.collectAsState()
            var showDismissMethodDialog by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDismissMethodDialog = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dismiss Method",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dismissMethod.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showDismissMethodDialog) {
                AlertDialog(
                    onDismissRequest = { showDismissMethodDialog = false },
                    title = { Text("Select Dismiss Method") },
                    text = {
                        Column {
                            DismissMethod.values().forEach { method ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setDismissMethod(method)
                                            showDismissMethodDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (method == dismissMethod),
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = method.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDismissMethodDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Math Difficulty (Only if Math is selected)
            if (dismissMethod == DismissMethod.MATH) {
                val mathDifficulty by viewModel.mathDifficulty.collectAsState()
                var showMathDifficultyDialog by remember { mutableStateOf(false) }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMathDifficultyDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Math Difficulty",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = mathDifficulty.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (showMathDifficultyDialog) {
                    AlertDialog(
                        onDismissRequest = { showMathDifficultyDialog = false },
                        title = { Text("Select Difficulty") },
                        text = {
                            Column {
                                MathDifficulty.values().forEach { difficulty ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setMathDifficulty(difficulty)
                                                showMathDifficultyDialog = false
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (difficulty == mathDifficulty),
                                            onClick = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = difficulty.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showMathDifficultyDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Snooze Duration
            val snoozeDuration by viewModel.snoozeDuration.collectAsState()
            var showSnoozeDurationDialog by remember { mutableStateOf(false) }
            val snoozeOptions = listOf(5, 10, 15, 20, 30)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSnoozeDurationDialog = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Snooze Duration",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$snoozeDuration minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showSnoozeDurationDialog) {
                AlertDialog(
                    onDismissRequest = { showSnoozeDurationDialog = false },
                    title = { Text("Select Snooze Duration") },
                    text = {
                        Column {
                            snoozeOptions.forEach { duration ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setSnoozeDuration(duration)
                                            showSnoozeDurationDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (duration == snoozeDuration),
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$duration minutes",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSnoozeDurationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Max Snooze Count
            val maxSnoozeCount by viewModel.maxSnoozeCount.collectAsState()
            var showMaxSnoozeDialog by remember { mutableStateOf(false) }
            val snoozeCountOptions = listOf(0, 1, 2, 3, 5, 10) // 0 means unlimited

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMaxSnoozeDialog = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Max Snooze Count",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (maxSnoozeCount == 0) "Unlimited" else "$maxSnoozeCount times",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showMaxSnoozeDialog) {
                AlertDialog(
                    onDismissRequest = { showMaxSnoozeDialog = false },
                    title = { Text("Select Max Snooze Count") },
                    text = {
                        Column {
                            snoozeCountOptions.forEach { count ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setMaxSnoozeCount(count)
                                            showMaxSnoozeDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (count == maxSnoozeCount),
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (count == 0) "Unlimited" else "$count times",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showMaxSnoozeDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Alexa Integration
            val isAlexaLinked by viewModel.isAlexaLinked.collectAsState()
            
            // Check status on composition
            androidx.compose.runtime.LaunchedEffect(Unit) {
                viewModel.checkAlexaLinkStatus(context)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Placeholder for Alexa Icon if not available, or use a generic icon
                    Icon(
                        imageVector = Icons.Filled.Info, // Using Info as placeholder for Alexa
                        contentDescription = "Alexa",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Link with Alexa",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAlexaLinked) "Connected to Alexa" else "Not connected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isAlexaLinked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = { 
                        if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                        onLinkAlexaClick() 
                    }
                ) {
                    Text(if (isAlexaLinked) "Disconnect" else "Connect")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Section: Accessibility
            SettingsSectionHeader(title = "Accessibility")

            // High Contrast Mode
            val highContrastMode by viewModel.highContrastMode.collectAsState()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Accessibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "High Contrast Mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Increase contrast for better visibility",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = highContrastMode,
                    onCheckedChange = { 
                        if (hapticFeedbackEnabled) HapticFeedback.performToggle(view)
                        viewModel.setHighContrastMode(it) 
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Haptic Feedback
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Vibration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Haptic Feedback",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vibrate on button presses",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = hapticFeedbackEnabled,
                    onCheckedChange = { newValue ->
                        // Always provide feedback when toggling, so user can feel the change
                        HapticFeedback.performToggle(view)
                        viewModel.setHapticFeedbackEnabled(newValue) 
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Section: About
            SettingsSectionHeader(title = "About")

            var showAboutDialog by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                        showAboutDialog = true 
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "About Clock",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Version 1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    title = { Text("About Clock") },
                    text = {
                        Column {
                            Text(
                                text = "Clock App",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Version 1.0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "A modern clock app with alarm, timer, stopwatch, and world clock features.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Built with Jetpack Compose and Material 3",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "© 2024 Suvojeet Sengupta",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
