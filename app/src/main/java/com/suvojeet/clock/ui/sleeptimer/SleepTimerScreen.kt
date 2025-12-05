package com.suvojeet.clock.ui.sleeptimer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.suvojeet.clock.service.SleepTimerService
import com.suvojeet.clock.util.HapticFeedback

@Composable
fun SleepTimerScreen(
    onBackClick: () -> Unit,
    viewModel: SleepTimerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()
    
    var sleepTimerService by remember { mutableStateOf<SleepTimerService?>(null) }
    var isServiceBound by remember { mutableStateOf(false) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val serviceBinder = binder as SleepTimerService.SleepTimerBinder
                sleepTimerService = serviceBinder.getService()
                isServiceBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                sleepTimerService = null
                isServiceBound = false
            }
        }
    }

    // Collect service state
    LaunchedEffect(sleepTimerService) {
        sleepTimerService?.let { service ->
            service.isPlaying.collect { isPlaying ->
                viewModel.updatePlayingState(isPlaying, service.remainingTime.value)
            }
        }
    }

    LaunchedEffect(sleepTimerService) {
        sleepTimerService?.let { service ->
            service.remainingTime.collect { remaining ->
                viewModel.updatePlayingState(service.isPlaying.value, remaining)
            }
        }
    }

    DisposableEffect(Unit) {
        val intent = Intent(context, SleepTimerService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        onDispose {
            if (isServiceBound) {
                context.unbindService(connection)
            }
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    HapticFeedback.performClick(view)
                    onBackClick() 
                }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Sleep Timer",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isPlaying) {
                // Timer Running View
                TimerRunningView(
                    remainingMs = uiState.remainingTimeMs,
                    selectedSound = uiState.selectedSound,
                    onStop = {
                        HapticFeedback.performClick(view)
                        sleepTimerService?.stopTimer()
                    }
                )
            } else {
                // Timer Setup View
                TimerSetupView(
                    uiState = uiState,
                    viewModel = viewModel,
                    onStart = {
                        HapticFeedback.performConfirm(view)
                        val intent = Intent(context, SleepTimerService::class.java)
                        context.startService(intent)
                        sleepTimerService?.startTimer(
                            durationMinutes = uiState.selectedDuration,
                            fadeSeconds = uiState.selectedFadeDuration,
                            soundName = uiState.selectedSound
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TimerRunningView(
    remainingMs: Long,
    selectedSound: String,
    onStop: () -> Unit
) {
    val minutes = (remainingMs / 60_000).toInt()
    val seconds = ((remainingMs % 60_000) / 1000).toInt()

    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Moon icon with glow
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6B5B95).copy(alpha = alpha),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Bedtime,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF9B8DC7)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Time Display
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 72.sp,
                fontWeight = FontWeight.Light
            ),
            color = Color.White
        )

        Text(
            text = "remaining",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sound indicator
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    getSoundIcon(selectedSound),
                    contentDescription = null,
                    tint = Color(0xFF9B8DC7)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedSound,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Stop Button
        Button(
            onClick = onStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Timer", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun TimerSetupView(
    uiState: SleepTimerUiState,
    viewModel: SleepTimerViewModel,
    onStart: () -> Unit
) {
    val view = LocalView.current
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Duration Selection
        Text(
            text = "Timer Duration",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.durationOptions) { duration ->
                DurationChip(
                    duration = duration,
                    isSelected = uiState.selectedDuration == duration,
                    onClick = { 
                        HapticFeedback.performClick(view)
                        viewModel.setDuration(duration) 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Fade Duration
        Text(
            text = "Fade Out Duration",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.fadeOptions) { fade ->
                FadeChip(
                    seconds = fade,
                    isSelected = uiState.selectedFadeDuration == fade,
                    onClick = { 
                        HapticFeedback.performClick(view)
                        viewModel.setFadeDuration(fade) 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sound Selection
        Text(
            text = "Sound",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.soundOptions.forEach { sound ->
                SoundCard(
                    soundName = sound,
                    isSelected = uiState.selectedSound == sound,
                    onClick = { 
                        HapticFeedback.performClick(view)
                        viewModel.setSound(sound) 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Start Button
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6B5B95),
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Bedtime, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Sleep Timer", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hint
        Text(
            text = "The sound will gradually fade as the timer ends",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun DurationChip(
    duration: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val label = if (duration >= 60) "${duration / 60}h" else "${duration}m"
    
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFF6B5B95), RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2D2640) else Color(0xFF1C1C1E)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = if (isSelected) Color(0xFF9B8DC7) else Color.White
        )
    }
}

@Composable
private fun FadeChip(
    seconds: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFF6B5B95), RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2D2640) else Color(0xFF1C1C1E)
        )
    ) {
        Text(
            text = "${seconds}s",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = if (isSelected) Color(0xFF9B8DC7) else Color.White
        )
    }
}

@Composable
private fun SoundCard(
    soundName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFF6B5B95), RoundedCornerShape(12.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2D2640) else Color(0xFF1C1C1E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                getSoundIcon(soundName),
                contentDescription = null,
                tint = if (isSelected) Color(0xFF9B8DC7) else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = soundName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF9B8DC7)
                )
            }
        }
    }
}

private fun getSoundIcon(soundName: String): ImageVector {
    return when (soundName) {
        "Default" -> Icons.Default.MusicNote
        "Notification" -> Icons.Default.Notifications
        "Alarm" -> Icons.Default.Alarm
        "Ringtone" -> Icons.Default.RingVolume
        else -> Icons.Default.MusicNote
    }
}
