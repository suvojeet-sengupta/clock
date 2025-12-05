package com.suvojeet.clock.ui.timer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Data class representing a preset timer option
 */
data class TimerPreset(
    val label: String,
    val minutes: Int,
    val seconds: Int = 0
)

/**
 * Common timer presets for quick selection
 */
private val timerPresets = listOf(
    TimerPreset("1 min", 1),
    TimerPreset("3 min", 3),
    TimerPreset("5 min", 5),
    TimerPreset("10 min", 10),
    TimerPreset("15 min", 15),
    TimerPreset("30 min", 30)
)

@Composable
fun TimerScreen(viewModel: TimerViewModel = hiltViewModel()) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    // Input states for the picker - these persist across timer runs
    var hoursInput by remember { mutableStateOf("00") }
    var minutesInput by remember { mutableStateOf("00") }
    var secondsInput by remember { mutableStateOf("00") }

    // Derived display values
    val displayHours = if (totalTime > 0) ((timeLeft / 1000) / 3600).toString().padStart(2, '0') else hoursInput
    val displayMinutes = if (totalTime > 0) (((timeLeft / 1000) % 3600) / 60).toString().padStart(2, '0') else minutesInput
    val displaySeconds = if (totalTime > 0) ((timeLeft / 1000) % 60).toString().padStart(2, '0') else secondsInput

    Scaffold(
        containerColor = Color.Black // Dark background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (totalTime > 0) "Timer Running" else "Set Timer",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TimeInput(
                        value = displayHours,
                        onValueChange = { if (it.length <= 2) hoursInput = it },
                        label = "h",
                        readOnly = totalTime > 0
                    )
                    Text(":", style = MaterialTheme.typography.displayMedium.copy(color = Color.Gray), modifier = Modifier.padding(horizontal = 8.dp))
                    TimeInput(
                        value = displayMinutes,
                        onValueChange = { if (it.length <= 2) minutesInput = it },
                        label = "m",
                        readOnly = totalTime > 0
                    )
                    Text(":", style = MaterialTheme.typography.displayMedium.copy(color = Color.Gray), modifier = Modifier.padding(horizontal = 8.dp))
                    TimeInput(
                        value = displaySeconds,
                        onValueChange = { if (it.length <= 2) secondsInput = it },
                        label = "s",
                        readOnly = totalTime > 0
                    )
                }

                // Quick preset buttons - only show when timer is not running
                if (totalTime == 0L) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Quick Start",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Two rows of 3 presets each
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            timerPresets.take(3).forEach { preset ->
                                PresetChip(
                                    preset = preset,
                                    onClick = {
                                        hoursInput = "00"
                                        minutesInput = preset.minutes.toString().padStart(2, '0')
                                        secondsInput = preset.seconds.toString().padStart(2, '0')
                                        viewModel.setTimer(0, preset.minutes, preset.seconds)
                                        viewModel.startTimer()
                                    }
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            timerPresets.drop(3).forEach { preset ->
                                PresetChip(
                                    preset = preset,
                                    onClick = {
                                        hoursInput = "00"
                                        minutesInput = preset.minutes.toString().padStart(2, '0')
                                        secondsInput = preset.seconds.toString().padStart(2, '0')
                                        viewModel.setTimer(0, preset.minutes, preset.seconds)
                                        viewModel.startTimer()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                if (totalTime == 0L) {
                    Button(
                        onClick = {
                            val h = hoursInput.toIntOrNull() ?: 0
                            val m = minutesInput.toIntOrNull() ?: 0
                            val s = secondsInput.toIntOrNull() ?: 0
                            if (h > 0 || m > 0 || s > 0) {
                                viewModel.setTimer(h, m, s)
                                viewModel.startTimer()
                            }
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .width(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2C3E50), // Dark Blue
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Start timer")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Button(
                            onClick = { if (isRunning) viewModel.pauseTimer() else viewModel.startTimer() },
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) Color(0xFFFFB74D) else Color(0xFF2C3E50),
                                contentColor = if (isRunning) Color.Black else Color.White
                            )
                        ) {
                            Icon(
                                if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, 
                                contentDescription = if (isRunning) "Pause timer" else "Resume timer"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isRunning) "Pause" else "Resume", style = MaterialTheme.typography.titleMedium)
                        }

                        Button(
                            onClick = { viewModel.stopTimer() },
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1C1C1E), // Dark gray
                                contentColor = Color(0xFFD32F2F)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop timer")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetChip(
    preset: TimerPreset,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = Color(0xFF1C1C1E),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = preset.label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun TimeInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    readOnly: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = value,
            onValueChange = { 
                // Only allow numeric input
                if (it.all { char -> char.isDigit() }) {
                    onValueChange(it) 
                }
            },
            readOnly = readOnly,
            modifier = Modifier
                .width(80.dp)
                .background(Color(0xFF1C1C1E), RoundedCornerShape(8.dp)), // Dark gray
            textStyle = TextStyle(
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = if (readOnly) Color.Transparent else Color(0xFF2C3E50),
                unfocusedContainerColor = Color(0xFF1C1C1E),
                focusedContainerColor = Color(0xFF1C1C1E)
            ),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
    }
}
