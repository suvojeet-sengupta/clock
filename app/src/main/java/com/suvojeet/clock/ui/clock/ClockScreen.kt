package com.suvojeet.clock.ui.clock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClockScreen() {
    val viewModel: ClockViewModel = hiltViewModel()
    
    val currentTime by viewModel.currentTime.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val clockStyle by viewModel.clockStyle.collectAsState()
    val nextAlarm by viewModel.nextAlarm.collectAsState()
    
    var showStyleSelector by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    // Format time for accessibility announcement
    val timeFormatter = DateTimeFormatter.ofPattern(if (is24HourFormat) "HH:mm" else "h:mm a")
    val accessibleTimeText = "Current time is ${currentTime.format(timeFormatter)}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // Use theme background
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .combinedClickable(
                    onClick = { },
                    onLongClick = { showStyleSelector = true },
                    onLongClickLabel = "Change clock style"
                )
                .semantics { contentDescription = accessibleTimeText }
        ) {
            AnalogClock(currentTime, clockStyle)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        DigitalClock(currentTime, is24HourFormat)
        
        // Show next alarm if available
        nextAlarm?.let { alarmInfo ->
            Spacer(modifier = Modifier.height(24.dp))
            NextAlarmIndicator(
                alarmInfo = alarmInfo,
                is24HourFormat = is24HourFormat
            )
        }
    }

    if (showStyleSelector) {
        ModalBottomSheet(
            onDismissRequest = { showStyleSelector = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ClockStyleSelector(
                currentStyle = clockStyle,
                onStyleSelected = { 
                    viewModel.updateClockStyle(it)
                    showStyleSelector = false
                }
            )
        }
    }
}

@Composable
fun NextAlarmIndicator(
    alarmInfo: NextAlarmInfo,
    is24HourFormat: Boolean
) {
    val timeFormatter = DateTimeFormatter.ofPattern(if (is24HourFormat) "HH:mm" else "h:mm a")
    val alarmTimeText = alarmInfo.nextTriggerTime.format(timeFormatter)
    val label = alarmInfo.alarm.label.ifEmpty { "Alarm" }
    

    Surface(
        modifier = Modifier
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), // Glassy surface
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⏰",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "$label • $alarmTimeText (${alarmInfo.timeUntilAlarm})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ClockStyleSelector(
    currentStyle: ClockStyle,
    onStyleSelected: (ClockStyle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Clock Style",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ClockStyle.values().forEach { style ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .combinedClickable(onClick = { onStyleSelected(style) })
                        .padding(8.dp)
                        .background(
                            if (currentStyle == style) MaterialTheme.colorScheme.primaryContainer 
                            else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    // Mini preview could go here, for now just text
                    Text(
                        text = style.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (currentStyle == style) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AnalogClock(time: LocalTime, style: ClockStyle) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val background = MaterialTheme.colorScheme.background
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(250.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background) // Theme gradient
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            when (style) {
                ClockStyle.CLASSIC -> DrawClassicClock(this, center, radius, time, primaryColor, onSurface, tertiaryColor, onSurface)
                ClockStyle.MINIMAL -> DrawMinimalClock(this, center, radius, time, primaryColor, secondaryColor)
                ClockStyle.NEON -> DrawNeonClock(this, center, radius, time, primaryColor, secondaryColor, tertiaryColor)
                ClockStyle.DOT -> DrawDotClock(this, center, radius, time, primaryColor, secondaryColor, tertiaryColor)
            }
        }
    }
}

fun DrawClassicClock(
    scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    center: Offset,
    radius: Float,
    time: LocalTime,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    onSurface: Color
) {
    with(scope) {
        // Ticks
        for (i in 0 until 60) {
            val angle = i * 6f
            val isHour = i % 5 == 0
            val length = if (isHour) 24.dp.toPx() else 12.dp.toPx()
            val color = if (isHour) primary else onSurface.copy(alpha = 0.3f)
            val strokeWidth = if (isHour) 4.dp.toPx() else 2.dp.toPx()

            val start = center + Offset(
                x = (radius - length) * cos(Math.toRadians(angle.toDouble() - 90)).toFloat(),
                y = (radius - length) * sin(Math.toRadians(angle.toDouble() - 90)).toFloat()
            )
            val end = center + Offset(
                x = radius * cos(Math.toRadians(angle.toDouble() - 90)).toFloat(),
                y = radius * sin(Math.toRadians(angle.toDouble() - 90)).toFloat()
            )

            drawLine(color, start, end, strokeWidth, StrokeCap.Round)
        }

        // Hands
        drawHands(this, center, radius, time, primary, secondary, tertiary, true)
    }
}

fun DrawMinimalClock(
    scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    center: Offset,
    radius: Float,
    time: LocalTime,
    primary: Color,
    secondary: Color
) {
    with(scope) {
        // Only 12, 3, 6, 9 ticks
        for (i in 0 until 4) {
            val angle = i * 90f
            val length = 30.dp.toPx()
            val start = center + Offset(
                x = (radius - length) * cos(Math.toRadians(angle.toDouble() - 90)).toFloat(),
                y = (radius - length) * sin(Math.toRadians(angle.toDouble() - 90)).toFloat()
            )
            val end = center + Offset(
                x = radius * cos(Math.toRadians(angle.toDouble() - 90)).toFloat(),
                y = radius * sin(Math.toRadians(angle.toDouble() - 90)).toFloat()
            )
            drawLine(primary, start, end, 6.dp.toPx(), StrokeCap.Round)
        }
        
        // Simple hands
        val hourAngle = (time.hour % 12 + time.minute / 60f) * 30f
        rotate(hourAngle) {
            drawLine(primary, center, center - Offset(0f, radius * 0.5f), 12.dp.toPx(), StrokeCap.Round)
        }
        val minuteAngle = (time.minute + time.second / 60f) * 6f
        rotate(minuteAngle) {
            drawLine(secondary, center, center - Offset(0f, radius * 0.7f), 8.dp.toPx(), StrokeCap.Round)
        }
    }
}

fun DrawNeonClock(
    scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    center: Offset,
    radius: Float,
    time: LocalTime,
    primary: Color,
    secondary: Color,
    tertiary: Color
) {
    with(scope) {
        // Outer ring
        drawCircle(primary, radius, center, style = Stroke(width = 4.dp.toPx()))
        
        // Hands with glow effect (simulated by transparency)
        val hourAngle = (time.hour % 12 + time.minute / 60f) * 30f
        rotate(hourAngle) {
            drawLine(primary.copy(alpha = 0.5f), center, center - Offset(0f, radius * 0.5f), 16.dp.toPx(), StrokeCap.Round)
            drawLine(primary, center, center - Offset(0f, radius * 0.5f), 8.dp.toPx(), StrokeCap.Round)
        }
        val minuteAngle = (time.minute + time.second / 60f) * 6f
        rotate(minuteAngle) {
            drawLine(secondary.copy(alpha = 0.5f), center, center - Offset(0f, radius * 0.7f), 12.dp.toPx(), StrokeCap.Round)
            drawLine(secondary, center, center - Offset(0f, radius * 0.7f), 6.dp.toPx(), StrokeCap.Round)
        }
        val secondAngle = time.second * 6f
        rotate(secondAngle) {
            drawLine(tertiary, center, center - Offset(0f, radius * 0.85f), 4.dp.toPx(), StrokeCap.Round)
        }
    }
}

fun DrawDotClock(
    scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    center: Offset,
    radius: Float,
    time: LocalTime,
    primary: Color,
    secondary: Color,
    tertiary: Color
) {
    with(scope) {
        // Dots for hours
        for (i in 0 until 12) {
            val angle = i * 30f
            val pos = center + Offset(
                x = (radius - 20.dp.toPx()) * cos(Math.toRadians(angle.toDouble() - 90)).toFloat(),
                y = (radius - 20.dp.toPx()) * sin(Math.toRadians(angle.toDouble() - 90)).toFloat()
            )
            drawCircle(primary.copy(alpha = 0.5f), 6.dp.toPx(), pos)
        }

        // Hands as dots
        val hourAngle = (time.hour % 12 + time.minute / 60f) * 30f
        val hourPos = center + Offset(
            x = (radius * 0.5f) * cos(Math.toRadians(hourAngle.toDouble() - 90)).toFloat(),
            y = (radius * 0.5f) * sin(Math.toRadians(hourAngle.toDouble() - 90)).toFloat()
        )
        drawCircle(primary, 12.dp.toPx(), hourPos)

        val minuteAngle = (time.minute + time.second / 60f) * 6f
        val minutePos = center + Offset(
            x = (radius * 0.7f) * cos(Math.toRadians(minuteAngle.toDouble() - 90)).toFloat(),
            y = (radius * 0.7f) * sin(Math.toRadians(minuteAngle.toDouble() - 90)).toFloat()
        )
        drawCircle(secondary, 10.dp.toPx(), minutePos)

        val secondAngle = time.second * 6f
        val secondPos = center + Offset(
            x = (radius * 0.85f) * cos(Math.toRadians(secondAngle.toDouble() - 90)).toFloat(),
            y = (radius * 0.85f) * sin(Math.toRadians(secondAngle.toDouble() - 90)).toFloat()
        )
        drawCircle(tertiary, 8.dp.toPx(), secondPos)
    }
}

fun drawHands(
    scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    center: Offset,
    radius: Float,
    time: LocalTime,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    drawSecondHand: Boolean
) {
    with(scope) {
        // Hour Hand
        val hourAngle = (time.hour % 12 + time.minute / 60f) * 30f
        rotate(hourAngle) {
            drawLine(primary, center, center - Offset(0f, radius * 0.5f), 10.dp.toPx(), StrokeCap.Round)
        }

        // Minute Hand
        val minuteAngle = (time.minute + time.second / 60f) * 6f
        rotate(minuteAngle) {
            drawLine(secondary, center, center - Offset(0f, radius * 0.7f), 6.dp.toPx(), StrokeCap.Round)
        }

        // Second Hand
        if (drawSecondHand) {
            val secondAngle = time.second * 6f
            rotate(secondAngle) {
                drawLine(tertiary, center, center - Offset(0f, radius * 0.85f), 3.dp.toPx(), StrokeCap.Round)
            }
            drawCircle(tertiary, 8.dp.toPx(), center)
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

