package com.suvojeet.clock.ui.alarm

import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as ClockApplication
    val repository = AlarmRepository(application.database.alarmDao())
    val viewModel: AlarmViewModel = viewModel(factory = AlarmViewModelFactory(repository, application.applicationContext))
    
    val alarms by viewModel.allAlarms.collectAsState()
    
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val scope = rememberCoroutineScope()
    val fabCornerSize = remember { androidx.compose.animation.core.Animatable(24f) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    scope.launch {
                        // Morph to square (faster)
                        fabCornerSize.animateTo(
                            targetValue = 4f,
                            animationSpec = androidx.compose.animation.core.tween(150)
                        )
                        // Open sheet immediately after squish
                        selectedAlarm = null
                        showBottomSheet = true 
                        
                        // Morph back to original concurrently with sheet opening
                        fabCornerSize.animateTo(
                            targetValue = 24f,
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                            )
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(fabCornerSize.value.dp)
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
                    val displayTime = try {
                        LocalTime.parse(alarm.time).format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
                    } catch (e: Exception) {
                        alarm.time // Fallback or already formatted
                    }
                    Text(
                        text = displayTime.uppercase(),
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
        try {
            LocalTime.parse(alarm.time)
        } catch (e: Exception) {
            LocalTime.parse(alarm.time, DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
        }
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
                        val formatter = DateTimeFormatter.ofPattern("HH:mm")
                        onSave(
                            AlarmEntity(
                                id = alarm?.id ?: 0,
                                time = time.format(formatter),
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
