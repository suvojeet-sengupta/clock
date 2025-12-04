package com.suvojeet.clock.ui.timer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun TimerScreen(viewModel: TimerViewModel = hiltViewModel()) {
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
        targetValue = if (isRunning) 1.05f else 1.0f, // Only pulse when running
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = "Scale"
    )

    Scaffold(
        containerColor = Color.Black // Dark background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TimeInput(value = hoursInput, onValueChange = { if (it.length <= 2) hoursInput = it }, label = "h")
                        Text(":", style = MaterialTheme.typography.displayMedium.copy(color = Color.Gray), modifier = Modifier.padding(horizontal = 8.dp))
                        TimeInput(value = minutesInput, onValueChange = { if (it.length <= 2) minutesInput = it }, label = "m")
                        Text(":", style = MaterialTheme.typography.displayMedium.copy(color = Color.Gray), modifier = Modifier.padding(horizontal = 8.dp))
                        TimeInput(value = secondsInput, onValueChange = { if (it.length <= 2) secondsInput = it }, label = "s")
                    }

                    Spacer(modifier = Modifier.height(64.dp))

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
                        .size(280.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape)
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
                            fontWeight = FontWeight.Light,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(64.dp))

                    // Controls
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
                            Icon(if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isRunning) "Pause" else "Resume", style = MaterialTheme.typography.titleMedium)
                        }

                        Button(
                            onClick = { viewModel.resetTimer() },
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
                            Icon(Icons.Filled.Stop, contentDescription = null)
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
                focusedBorderColor = Color(0xFF2C3E50),
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
