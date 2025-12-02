package com.suvojeet.clock.ui.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suvojeet.clock.ui.theme.ElectricBlue
import com.suvojeet.clock.ui.theme.NebulaPurple
import com.suvojeet.clock.ui.theme.NeonPink

@Composable
fun TimerScreen(viewModel: TimerViewModel = viewModel()) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (totalTime == 0L) {
            // Simple setup for demo purposes - hardcoded 1 minute timer setup
            Button(onClick = { viewModel.setTimer(0, 1, 0) }) {
                Text("Set 1 Minute Timer")
            }
        } else {
            val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "Progress")

            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(300.dp)) {
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = if (timeLeft < 10000) NeonPink else ElectricBlue,
                        startAngle = -90f,
                        sweepAngle = 360 * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                val hours = (timeLeft / 1000) / 3600
                val minutes = ((timeLeft / 1000) % 3600) / 60
                val seconds = (timeLeft / 1000) % 60
                
                Text(
                    text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row {
                Button(
                    onClick = { if (isRunning) viewModel.pauseTimer() else viewModel.startTimer() },
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start"
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { viewModel.resetTimer() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                }
            }
        }
    }
}
