package com.suvojeet.clock.ui.alarm

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suvojeet.clock.ClockApplication
import com.suvojeet.clock.data.alarm.AlarmEntity
import com.suvojeet.clock.data.alarm.AlarmRepository
import com.suvojeet.clock.ui.theme.ElectricBlue
import com.suvojeet.clock.ui.theme.GlassSurface
import com.suvojeet.clock.ui.theme.NebulaPurple
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun AlarmScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as ClockApplication
    val repository = AlarmRepository(application.database.alarmDao())
    val viewModel: AlarmViewModel = viewModel(factory = AlarmViewModelFactory(repository))
    
    val alarms by viewModel.allAlarms.collectAsState()
    
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val time = LocalTime.of(selectedHour, selectedMinute)
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                viewModel.addAlarm(AlarmEntity(time = time.format(formatter)))
                showTimePicker = false
            },
            hour,
            minute,
            true
        ).show()
        showTimePicker = false // Reset immediately as dialog handles its own state, but cleaner to handle dismiss
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)

                    TimePickerDialog(
                        context,
                        { _, selectedHour, selectedMinute ->
                            val time = LocalTime.of(selectedHour, selectedMinute)
                            val formatter = DateTimeFormatter.ofPattern("HH:mm")
                            viewModel.addAlarm(AlarmEntity(time = time.format(formatter)))
                        },
                        hour,
                        minute,
                        true
                    ).show()
                },
                containerColor = NebulaPurple,
                contentColor = Color.White
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(alarms) { alarm ->
                    AlarmItem(
                        alarm = alarm,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onDelete = { viewModel.deleteAlarm(alarm) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmItem(
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = alarm.time,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
                if (alarm.label.isNotEmpty()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NebulaPurple,
                        checkedTrackColor = NebulaPurple.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }
    }
}
