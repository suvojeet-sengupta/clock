package com.suvojeet.clock.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val appTheme by viewModel.appTheme.collectAsState()
    val gradualVolume by viewModel.gradualVolume.collectAsState()
    val dismissMethod by viewModel.dismissMethod.collectAsState()
    val mathDifficulty by viewModel.mathDifficulty.collectAsState()
    val snoozeDuration by viewModel.snoozeDuration.collectAsState()
    val maxSnoozeCount by viewModel.maxSnoozeCount.collectAsState()
    val highContrastMode by viewModel.highContrastMode.collectAsState()
    val isAlexaLinked by viewModel.isAlexaLinked.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showDismissMethodDialog by remember { mutableStateOf(false) }
    var showMathDifficultyDialog by remember { mutableStateOf(false) }
    var showSnoozeDurationDialog by remember { mutableStateOf(false) }
    var showMaxSnoozeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Check Alexa status
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.checkAlexaLinkStatus(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Display Section
            SettingsSection(title = "Display") {
                SettingsCard {
                    SettingsToggleItem(
                        icon = Icons.Default.AccessTime,
                        title = "24-Hour Format",
                        subtitle = "Use 24-hour time display",
                        checked = is24HourFormat,
                        onCheckedChange = {
                            if (hapticFeedbackEnabled) HapticFeedback.performToggle(view)
                            viewModel.set24HourFormat(it)
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    
                    SettingsClickableItem(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = appTheme.displayName,
                        onClick = {
                            if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                            showThemeDialog = true
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    
                    SettingsClickableItem(
                        icon = Icons.Default.Bedtime,
                        title = "Sleep Timer",
                        subtitle = "Fall asleep with soothing sounds",
                        onClick = {
                            if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                            onSleepTimerClick()
                        }
                    )
                }
            }

            // Alarm Section
            SettingsSection(title = "Alarm") {
                SettingsCard {
                    SettingsToggleItem(
                        icon = Icons.Default.VolumeUp,
                        title = "Gradual Volume",
                        subtitle = "Increase volume over 30 seconds",
                        checked = gradualVolume,
                        onCheckedChange = {
                            if (hapticFeedbackEnabled) HapticFeedback.performToggle(view)
                            viewModel.setGradualVolume(it)
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    
                    SettingsClickableItem(
                        icon = Icons.Default.TouchApp,
                        title = "Dismiss Method",
                        subtitle = dismissMethod.name,
                        onClick = {
                            if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                            showDismissMethodDialog = true
                        }
                    )
                    
                    if (dismissMethod == DismissMethod.MATH) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        
                        SettingsClickableItem(
                            icon = Icons.Default.Speed,
                            title = "Math Difficulty",
                            subtitle = mathDifficulty.name,
                            onClick = {
                                if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                                showMathDifficultyDialog = true
                            }
                        )
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    
                    SettingsClickableItem(
                        icon = Icons.Default.Snooze,
                        title = "Snooze Duration",
                        subtitle = "$snoozeDuration minutes",
                        onClick = {
                            if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                            showSnoozeDurationDialog = true
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    
                    SettingsClickableItem(
                        icon = Icons.Default.Alarm,
                        title = "Max Snooze Count",
                        subtitle = if (maxSnoozeCount == 0) "Unlimited" else "$maxSnoozeCount times",
                        onClick = {
                            if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                            showMaxSnoozeDialog = true
                        }
                    )
                }
            }

            // Integrations Section
            SettingsSection(title = "Integrations") {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alexa",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAlexaLinked) "Connected" else "Not connected",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isAlexaLinked) MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        FilledTonalButton(
                            onClick = {
                                if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                                onLinkAlexaClick()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isAlexaLinked) 
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(if (isAlexaLinked) "Disconnect" else "Connect")
                        }
                    }
                }
            }

            // Accessibility Section
            SettingsSection(title = "Accessibility") {
                SettingsCard {
                    SettingsToggleItem(
                        icon = Icons.Default.Accessibility,
                        title = "High Contrast",
                        subtitle = "Increase contrast for visibility",
                        checked = highContrastMode,
                        onCheckedChange = {
                            if (hapticFeedbackEnabled) HapticFeedback.performToggle(view)
                            viewModel.setHighContrastMode(it)
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Feedback",
                        subtitle = "Vibrate on interactions",
                        checked = hapticFeedbackEnabled,
                        onCheckedChange = {
                            HapticFeedback.performToggle(view)
                            viewModel.setHapticFeedbackEnabled(it)
                        }
                    )
                }
            }

            // About Section
            SettingsSection(title = "About") {
                SettingsCard {
                    SettingsClickableItem(
                        icon = Icons.Default.Info,
                        title = "About Clock",
                        subtitle = "Version 1.0",
                        onClick = {
                            if (hapticFeedbackEnabled) HapticFeedback.performClick(view)
                            showAboutDialog = true
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showThemeDialog) {
        SelectionDialog(
            title = "Select Theme",
            options = AppTheme.values().map { it.displayName },
            selectedIndex = AppTheme.values().indexOf(appTheme),
            onSelect = { index ->
                viewModel.setAppTheme(AppTheme.values()[index])
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showDismissMethodDialog) {
        SelectionDialog(
            title = "Dismiss Method",
            options = DismissMethod.values().map { it.name },
            selectedIndex = DismissMethod.values().indexOf(dismissMethod),
            onSelect = { index ->
                viewModel.setDismissMethod(DismissMethod.values()[index])
                showDismissMethodDialog = false
            },
            onDismiss = { showDismissMethodDialog = false }
        )
    }

    if (showMathDifficultyDialog) {
        SelectionDialog(
            title = "Math Difficulty",
            options = MathDifficulty.values().map { it.name },
            selectedIndex = MathDifficulty.values().indexOf(mathDifficulty),
            onSelect = { index ->
                viewModel.setMathDifficulty(MathDifficulty.values()[index])
                showMathDifficultyDialog = false
            },
            onDismiss = { showMathDifficultyDialog = false }
        )
    }

    if (showSnoozeDurationDialog) {
        val options = listOf(5, 10, 15, 20, 30)
        SelectionDialog(
            title = "Snooze Duration",
            options = options.map { "$it minutes" },
            selectedIndex = options.indexOf(snoozeDuration).coerceAtLeast(0),
            onSelect = { index ->
                viewModel.setSnoozeDuration(options[index])
                showSnoozeDurationDialog = false
            },
            onDismiss = { showSnoozeDurationDialog = false }
        )
    }

    if (showMaxSnoozeDialog) {
        val options = listOf(0, 1, 2, 3, 5, 10)
        SelectionDialog(
            title = "Max Snooze Count",
            options = options.map { if (it == 0) "Unlimited" else "$it times" },
            selectedIndex = options.indexOf(maxSnoozeCount).coerceAtLeast(0),
            onSelect = { index ->
                viewModel.setMaxSnoozeCount(options[index])
                showMaxSnoozeDialog = false
            },
            onDismiss = { showMaxSnoozeDialog = false }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Clock",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Version 1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "A modern, feature-rich clock app with alarms, timers, stopwatch, and world clock.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⏰",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Alarms",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⏱️",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Timer",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🌍",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "World",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⏲️",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Stopwatch",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Built with ❤️ using Jetpack Compose",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "© 2025 Suvojeet Sengupta",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = { showAboutDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = { onSelect(index) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
