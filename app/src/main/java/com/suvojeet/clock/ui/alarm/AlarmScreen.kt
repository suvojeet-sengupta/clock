package com.suvojeet.clock.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suvojeet.clock.ClockApplication
import com.suvojeet.clock.data.alarm.AlarmEntity
import com.suvojeet.clock.data.alarm.AlarmRepository
import com.suvojeet.clock.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as ClockApplication
    val repository = AlarmRepository(application.database.alarmDao())
    val viewModel: AlarmViewModel = viewModel(factory = AlarmViewModelFactory(repository))
    
    val alarms by viewModel.allAlarms.collectAsState()
    
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    selectedAlarm = null // New alarm
                    showBottomSheet = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Alarm")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (alarms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No alarms set",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms) { alarm ->
                        AlarmItem(
                            alarm = alarm,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onClick = {
                                selectedAlarm = alarm
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }

        if (showBottomSheet) {
            AlarmBottomSheet(
                alarm = selectedAlarm,
                onDismiss = { showBottomSheet = false },
                onSave = { alarm ->
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
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = alarm.time,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // Parse time to check AM/PM if needed, or just rely on 24h format for now.
                    // Ideally we should format based on system preference.
                }
                
                if (alarm.label.isNotEmpty() || alarm.daysOfWeek.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            if (alarm.label.isNotEmpty()) append(alarm.label)
                            if (alarm.label.isNotEmpty() && alarm.daysOfWeek.isNotEmpty()) append(" • ")
                            if (alarm.daysOfWeek.isNotEmpty()) {
                                append(formatDays(alarm.daysOfWeek))
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
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
        LocalTime.parse(alarm.time, DateTimeFormatter.ofPattern("HH:mm"))
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Settings Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { /* TODO: Open Label Dialog */ }
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
                        onClick = { onDelete(alarm) },
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
                        val formatter = DateTimeFormatter.ofPattern("HH:mm")
                        onSave(
                            AlarmEntity(
                                id = alarm?.id ?: 0,
                                time = time.format(formatter),
                                label = label,
                                isVibrateEnabled = isVibrateEnabled,
                                daysOfWeek = selectedDays,
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
    onDaySelected: (Int) -> Unit
) {
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    // 1 = Monday, 7 = Sunday in java.time.DayOfWeek, but let's map 1..7 to Mon..Sun
    // Or just use 0..6 indices. Let's assume 1=Mon, 7=Sun for simplicity in storage
    // But UI shows S M T W T F S (Sun Mon Tue...)
    // Let's map: Sun=7, Mon=1, Tue=2, ... Sat=6.
    val dayValues = listOf(7, 1, 2, 3, 4, 5, 6)
    
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

fun formatDays(days: List<Int>): String {
    if (days.size == 7) return "Every day"
    if (days.isEmpty()) return "Never"
    // Sort: Mon(1)..Sun(7). But we might want Sun first?
    // Let's just sort numerically 1..7
    val sortedDays = days.sorted()
    // Map to short names
    val dayNames = mapOf(
        1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun"
    )
    return sortedDays.joinToString(", ") { dayNames[it] ?: "" }
}
