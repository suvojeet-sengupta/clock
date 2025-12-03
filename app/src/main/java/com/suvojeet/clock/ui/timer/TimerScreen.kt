package com.suvojeet.clock.ui.timer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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

@Composable
fun TimerScreen(viewModel: TimerViewModel = viewModel()) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    // Input states for the picker
    var hoursInput by remember { mutableStateOf("00") }
    var minutesInput by remember { mutableStateOf("00") }
    var secondsInput by remember { mutableStateOf("00") }

    // Pulsing animation for running state
    val infiniteTransition = rememberInfiniteTransition(label = "Pulsing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.1f else 1.0f, // Only pulse when running
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = "Scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (totalTime == 0L) {
            // IDLE STATE: Time Picker
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Set Timer",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TimeInput(value = hoursInput, onValueChange = { if (it.length <= 2) hoursInput = it }, label = "h")
                    Text(":", style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(horizontal = 8.dp))
                    TimeInput(value = minutesInput, onValueChange = { if (it.length <= 2) minutesInput = it }, label = "m")
                    Text(":", style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(horizontal = 8.dp))
                    TimeInput(value = secondsInput, onValueChange = { if (it.length <= 2) secondsInput = it }, label = "s")
                }

                Spacer(modifier = Modifier.height(48.dp))

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
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            // RUNNING/PAUSED STATE
            // Background pulsing circle
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Time Display
                val hours = (timeLeft / 1000) / 3600
                val minutes = ((timeLeft / 1000) % 3600) / 60
                val seconds = (timeLeft / 1000) % 60

                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Thin
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { if (isRunning) viewModel.pauseTimer() else viewModel.startTimer() },
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                            contentColor = if (isRunning) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRunning) "Pause" else "Resume", style = MaterialTheme.typography.titleMedium)
                    }

                    Button(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun TimeInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
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
            modifier = Modifier.width(80.dp),
            textStyle = TextStyle(
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
