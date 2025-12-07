package com.suvojeet.clock.ui.alarm

import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suvojeet.clock.ClockApplication
import com.suvojeet.clock.data.alarm.AlarmEntity
import com.suvojeet.clock.data.alarm.AlarmRepository
import com.suvojeet.clock.ui.theme.*
import com.suvojeet.clock.util.HapticFeedback
import com.suvojeet.clock.util.TimeFormatter
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen() {
    val viewModel: AlarmViewModel = hiltViewModel()
    val view = LocalView.current
    
    val alarms by viewModel.allAlarms.collectAsState()
    
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedAlarm by remember { mutableStateOf<AlarmUiModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Check for Exact Alarm permission on Android 12+ (API 31+)
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
                    if (!alarmManager.canScheduleExactAlarms()) {
                        showPermissionDialog = true
                    } else {
                        showPermissionDialog = false
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text("To ensure your alarms ring precisely on time, please allow 'Alarms & Reminders' permission for this app.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                    }
                }) { Text("Settings") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background // Theme background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Alarm Clock",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                if (alarms.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No alarms set",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom button
                    ) {
                        items(alarms) { alarm ->
                            AlarmItem(
                                alarm = alarm,
                                onToggle = { 
                                    HapticFeedback.performToggle(view)
                                    viewModel.toggleAlarm(alarm.originalEntity) 
                                },
                                onClick = {
                                    HapticFeedback.performClick(view)
                                    selectedAlarm = alarm
                                    showBottomSheet = true
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Button
            Button(
                onClick = {
                    HapticFeedback.performClick(view)
                    selectedAlarm = null
                    showBottomSheet = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = "Set New Alarm",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        if (showBottomSheet) {
            AlarmBottomSheet(
                alarm = selectedAlarm?.originalEntity,
                onDismiss = { showBottomSheet = false },
                onSave = { alarm ->
                    HapticFeedback.performConfirm(view)
                    if (alarm.id == 0) {
                        viewModel.addAlarm(alarm)
                    } else {
                        viewModel.updateAlarm(alarm)
                    }
                    showBottomSheet = false
                },
                onDelete = { alarm ->
                    viewModel.deleteAlarm(alarm)
                    showBottomSheet = false
                },
                sheetState = sheetState
            )
        }
    }
}

@Composable
fun AlarmItem(
    alarm: AlarmUiModel,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) // Glassy
        ),
        
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = alarm.timeDisplay,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (alarm.label.isNotEmpty()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    text = alarm.daysDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmBottomSheet(
    alarm: AlarmEntity?,
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit,
    onDelete: (AlarmEntity) -> Unit,
    sheetState: SheetState
) {
    val initialTime = if (alarm != null) {
        TimeFormatter.parseDbTime(alarm.time)
    } else {
        LocalTime.now()
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )
    
    var label by remember { mutableStateOf(alarm?.label ?: "") }
    var isVibrateEnabled by remember { mutableStateOf(alarm?.isVibrateEnabled ?: true) }
    var selectedDays by remember { mutableStateOf(alarm?.daysOfWeek ?: emptyList()) }
    var soundUri by remember { mutableStateOf(alarm?.soundUri ?: "") }
    var showLabelDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val ringtoneLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: android.net.Uri? = result.data?.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            soundUri = uri?.toString() ?: ""
        }
    }

    if (showLabelDialog) {
        AlertDialog(
            onDismissRequest = { showLabelDialog = false },
            title = { Text("Alarm Label") },
            text = {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { showLabelDialog = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLabelDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Alarm") },
            text = { Text("Are you sure you want to delete this alarm?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (alarm != null) {
                            onDelete(alarm)
                        }
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimePicker(state = timePickerState)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            DaySelector(
                selectedDays = selectedDays,
                onDaySelected = { day ->
                    selectedDays = if (selectedDays.contains(day)) {
                        selectedDays - day
                    } else {
                        selectedDays + day
                    }
                },
                onQuickSelect = { days ->
                    selectedDays = days
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Settings Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLabelDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Label",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Text(
                        text = label.ifEmpty { "Alarm" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sound
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = android.content.Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_ALARM)
                                putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
                                putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, if (soundUri.isNotEmpty()) android.net.Uri.parse(soundUri) else null)
                                putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            }
                            ringtoneLauncher.launch(intent)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Audiotrack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Sound",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    var soundName by remember { mutableStateOf("Default") }
                    LaunchedEffect(soundUri) {
                        soundName = if (soundUri.isNotEmpty()) {
                            withContext(Dispatchers.IO) {
                                try {
                                    val uri = Uri.parse(soundUri)
                                    RingtoneManager.getRingtone(context, uri).getTitle(context)
                                } catch (e: Exception) {
                                    "Unknown"
                                }
                            }
                        } else {
                            "Default"
                        }
                    }
                    Text(
                        text = soundName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Vibrate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Vibrate",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Switch(
                        checked = isVibrateEnabled,
                        onCheckedChange = { isVibrateEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (alarm != null) {
                    TextButton(
                        onClick = { showDeleteConfirmation = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                } else {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
                
                Button(
                    onClick = {
                        val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        // Use TimeFormatter for consistent DB format
                        onSave(
                            AlarmEntity(
                                id = alarm?.id ?: 0,
                                time = TimeFormatter.formatTimeForDb(time),
                                label = label,
                                isVibrateEnabled = isVibrateEnabled,
                                daysOfWeek = selectedDays,
                                soundUri = soundUri,
                                isEnabled = true
                            )
                        )
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun DaySelector(
    selectedDays: List<Int>,
    onDaySelected: (Int) -> Unit,
    onQuickSelect: (List<Int>) -> Unit = {}
) {
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    val dayValues = listOf(7, 1, 2, 3, 4, 5, 6)
    
    val weekdays = listOf(1, 2, 3, 4, 5) // Mon-Fri
    val weekends = listOf(6, 7) // Sat-Sun
    val everyday = listOf(1, 2, 3, 4, 5, 6, 7)
    
    Column {
        // Quick select buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isWeekdaysSelected = selectedDays.sorted() == weekdays.sorted()
            val isWeekendsSelected = selectedDays.sorted() == weekends.sorted()
            val isEverydaySelected = selectedDays.sorted() == everyday.sorted()
            
            FilterChip(
                selected = isWeekdaysSelected,
                onClick = { 
                    if (isWeekdaysSelected) {
                        onQuickSelect(emptyList()) // Reset/clear
                    } else {
                        onQuickSelect(weekdays)
                    }
                },
                label = { Text("Weekdays") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isWeekendsSelected,
                onClick = { 
                    if (isWeekendsSelected) {
                        onQuickSelect(emptyList()) // Reset/clear
                    } else {
                        onQuickSelect(weekends)
                    }
                },
                label = { Text("Weekends") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isEverydaySelected,
                onClick = { 
                    if (isEverydaySelected) {
                        onQuickSelect(emptyList()) // Reset/clear
                    } else {
                        onQuickSelect(everyday)
                    }
                },
                label = { Text("Everyday") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Individual day selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { index, dayLabel ->
                val dayValue = dayValues[index]
                val isSelected = selectedDays.contains(dayValue)
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                            else Color.Transparent
                        )
                        .clickable { onDaySelected(dayValue) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}