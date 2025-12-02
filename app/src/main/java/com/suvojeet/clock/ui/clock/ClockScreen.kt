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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suvojeet.clock.ui.theme.ElectricBlue
import com.suvojeet.clock.ui.theme.NebulaPurple
import com.suvojeet.clock.ui.theme.NeonPink
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClockScreen(viewModel: ClockViewModel = viewModel()) {
    val currentTime by viewModel.currentTime.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnalogClock(currentTime)
        Spacer(modifier = Modifier.height(32.dp))
        DigitalClock(currentTime)
    }
}

@Composable
fun AnalogClock(time: LocalTime) {
    Box(
        modifier = Modifier
            .size(300.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
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
                val length = if (isHour) 20.dp.toPx() else 10.dp.toPx()
                val color = if (isHour) ElectricBlue else Color.Gray.copy(alpha = 0.5f)
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
                    color = NebulaPurple,
                    start = center,
                    end = center - Offset(0f, radius * 0.5f),
                    strokeWidth = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Minute Hand
            val minuteAngle = (time.minute + time.second / 60f) * 6f
            rotate(minuteAngle) {
                drawLine(
                    color = ElectricBlue,
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
                    color = NeonPink,
                    start = center,
                    end = center - Offset(0f, radius * 0.85f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Center Dot
            drawCircle(
                color = NeonPink,
                radius = 6.dp.toPx(),
                center = center
            )
        }
    }
}

@Composable
fun DigitalClock(time: LocalTime) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    Text(
        text = time.format(formatter),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}
