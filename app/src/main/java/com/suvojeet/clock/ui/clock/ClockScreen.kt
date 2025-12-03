package com.suvojeet.clock.ui.clock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suvojeet.clock.ClockApplication
import androidx.compose.ui.platform.LocalContext
import com.suvojeet.clock.ui.theme.ElectricBlue
import com.suvojeet.clock.ui.theme.NebulaPurple
import com.suvojeet.clock.ui.theme.NeonPink
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClockScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as ClockApplication
    val viewModel: ClockViewModel = viewModel(factory = ClockViewModelFactory(application.settingsRepository))
    
    val currentTime by viewModel.currentTime.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnalogClock(currentTime)
        Spacer(modifier = Modifier.height(32.dp))
        DigitalClock(currentTime, is24HourFormat)
    }
}

@Composable
fun AnalogClock(time: LocalTime) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(250.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            // Draw ticks
            for (i in 0 until 60) {
                val angle = i * 6f
                val isHour = i % 5 == 0
                val length = if (isHour) 24.dp.toPx() else 12.dp.toPx() // Longer ticks
                val color = if (isHour) primaryColor else onSurfaceColor.copy(alpha = 0.3f)
                val strokeWidth = if (isHour) 4.dp.toPx() else 2.dp.toPx()

                val start = center + Offset(
                    x = (radius - length) * cos(Math.toRadians(angle.toDouble() - 90)).toFloat(),
                    y = (radius - length) * sin(Math.toRadians(angle.toDouble() - 90)).toFloat()
                )
                val end = center + Offset(
                    x = radius * cos(Math.toRadians(angle.toDouble() - 90)).toFloat(),
                    y = radius * sin(Math.toRadians(angle.toDouble() - 90)).toFloat()
                )

                drawLine(
                    color = color,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // Hour Hand
            val hourAngle = (time.hour % 12 + time.minute / 60f) * 30f
            rotate(hourAngle) {
                drawLine(
                    color = primaryColor,
                    start = center,
                    end = center - Offset(0f, radius * 0.5f),
                    strokeWidth = 10.dp.toPx(), // Thicker
                    cap = StrokeCap.Round
                )
            }

            // Minute Hand
            val minuteAngle = (time.minute + time.second / 60f) * 6f
            rotate(minuteAngle) {
                drawLine(
                    color = secondaryColor,
                    start = center,
                    end = center - Offset(0f, radius * 0.7f),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Second Hand
            val secondAngle = time.second * 6f
            rotate(secondAngle) {
                drawLine(
                    color = tertiaryColor,
                    start = center,
                    end = center - Offset(0f, radius * 0.85f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Center Dot
            drawCircle(
                color = tertiaryColor,
                radius = 8.dp.toPx(),
                center = center
            )
        }
    }
}

@Composable
fun DigitalClock(time: LocalTime, is24HourFormat: Boolean) {
    val pattern = if (is24HourFormat) "HH:mm:ss" else "hh:mm:ss a"
    val formatter = DateTimeFormatter.ofPattern(pattern)
    Text(
        text = time.format(formatter).uppercase(),
        style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
        color = MaterialTheme.colorScheme.onBackground
    )
}
