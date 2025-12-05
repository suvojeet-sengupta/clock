package com.suvojeet.clock.ui.alarm

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suvojeet.clock.ClockApplication
import com.suvojeet.clock.data.alarm.AlarmReceiver
import com.suvojeet.clock.data.settings.DismissMethod
import com.suvojeet.clock.data.settings.MathDifficulty
import com.suvojeet.clock.ui.theme.CosmicTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {
    @Inject
    lateinit var settingsRepository: com.suvojeet.clock.data.settings.SettingsRepository

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var volumeJob: kotlinx.coroutines.Job? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        turnScreenOnAndKeyguard()
        
        lifecycleScope.launch {
            val gradualVolume = settingsRepository.gradualVolume.first()
            val dismissMethod = settingsRepository.dismissMethod.first()
            val mathDifficulty = settingsRepository.mathDifficulty.first()
            val snoozeDuration = settingsRepository.snoozeDuration.first()
            
            startRinging(gradualVolume)

            val label = intent.getStringExtra("EXTRA_MESSAGE") ?: "Alarm"
            val soundUri = intent.getStringExtra("EXTRA_SOUND_URI")

            setContent {
                CosmicTheme {
                    AlarmTriggerScreen(
                        label = label,
                        dismissMethod = dismissMethod,
                        mathDifficulty = mathDifficulty,
                        snoozeDurationMinutes = snoozeDuration,
                        onDismiss = {
                            dismissAlarm()
                        },
                        onSnooze = {
                            snoozeAlarm(label, soundUri, snoozeDuration)
                        }
                    )
                }
            }
        }
    }

    private fun turnScreenOnAndKeyguard() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun startRinging(gradualVolume: Boolean) {
        val soundUriString = intent.getStringExtra("EXTRA_SOUND_URI")
        val uri = if (!soundUriString.isNullOrEmpty()) {
            android.net.Uri.parse(soundUriString)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
        
        try {
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
            }

            if (gradualVolume) {
                mediaPlayer?.setVolume(0.1f, 0.1f)
                mediaPlayer?.start()
                startGradualVolumeIncrease()
            } else {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()
        }

        if (intent.getBooleanExtra("EXTRA_VIBRATE", true)) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 1000, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }
    }

    private fun startGradualVolumeIncrease() {
        volumeJob = lifecycleScope.launch {
            for (i in 1..10) {
                kotlinx.coroutines.delay(3000)
                val volume = 0.1f + (i * 0.09f)
                mediaPlayer?.setVolume(volume, volume)
            }
        }
    }

    private fun stopRinging() {
        volumeJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        vibrator?.cancel()
    }

    private fun cancelNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun dismissAlarm() {
        stopRinging()
        cancelNotification()
        finish()
    }

    private fun snoozeAlarm(label: String, soundUri: String?, snoozeDurationMinutes: Int) {
        stopRinging()
        cancelNotification()

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", label)
            putExtra("EXTRA_SOUND_URI", soundUri)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeTime = System.currentTimeMillis() + snoozeDurationMinutes * 60 * 1000L
        
        val clockInfo = AlarmManager.AlarmClockInfo(snoozeTime, pendingIntent)
        alarmManager.setAlarmClock(clockInfo, pendingIntent)

        Toast.makeText(this, "Snoozed for $snoozeDurationMinutes minutes", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRinging()
    }
}

@Composable
fun AlarmTriggerScreen(
    label: String,
    dismissMethod: DismissMethod,
    mathDifficulty: MathDifficulty,
    snoozeDurationMinutes: Int,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var showMathDialog by remember { mutableStateOf(false) }
    var showShakeScreen by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = LocalTime.now()
            kotlinx.coroutines.delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Pulsing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
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
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Thin
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onSnooze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.Snooze, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Snooze (${snoozeDurationMinutes}m)", style = MaterialTheme.typography.titleMedium)
                }

                Button(
                    onClick = {
                        when (dismissMethod) {
                            DismissMethod.STANDARD -> onDismiss()
                            DismissMethod.MATH -> showMathDialog = true
                            DismissMethod.SHAKE -> showShakeScreen = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.AlarmOff, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dismiss", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showMathDialog) {
        MathChallengeDialog(
            difficulty = mathDifficulty,
            onSuccess = onDismiss,
            onCancel = { showMathDialog = false }
        )
    }

    if (showShakeScreen) {
        ShakeChallengeScreen(
            onSuccess = onDismiss,
            onCancel = { showShakeScreen = false }
        )
    }
}

@Composable
fun MathChallengeDialog(
    difficulty: MathDifficulty,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    
    val problem = remember { generateMathProblem(difficulty) }

    AlertDialog(
        onDismissRequest = { /* Prevent dismiss */ },
        title = { Text("Solve to Dismiss") },
        text = {
            Column {
                Text(
                    text = "${problem.first} ${problem.second} ${problem.third} = ?",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = answer,
                    onValueChange = { 
                        answer = it
                        error = false
                    },
                    label = { Text("Answer") },
                    isError = error,
                    singleLine = true
                )
                if (error) {
                    Text(
                        text = "Incorrect answer",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (answer == problem.fourth.toString()) {
                        onSuccess()
                    } else {
                        error = true
                        answer = ""
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Back")
            }
        }
    )
}

fun generateMathProblem(difficulty: MathDifficulty): Quad<Int, String, Int, Int> {
    val random = Random.Default
    val operator = if (random.nextBoolean()) "+" else "-"
    val num1: Int
    val num2: Int
    
    when (difficulty) {
        MathDifficulty.EASY -> {
            num1 = random.nextInt(1, 20)
            num2 = random.nextInt(1, 20)
        }
        MathDifficulty.MEDIUM -> {
            num1 = random.nextInt(20, 100)
            num2 = random.nextInt(20, 100)
        }
        MathDifficulty.HARD -> {
            num1 = random.nextInt(100, 500)
            num2 = random.nextInt(50, 200)
        }
    }
    
    val result = if (operator == "+") num1 + num2 else num1 - num2
    return Quad(num1, operator, num2, result)
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun ShakeChallengeScreen(
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var shakeCount by remember { mutableStateOf(0) }
    val targetShakes = 15
    
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        var lastUpdate: Long = 0
        var lastX = 0f
        var lastY = 0f
        var lastZ = 0f
        val shakeThreshold = 800

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val curTime = System.currentTimeMillis()
                if ((curTime - lastUpdate) > 100) {
                    val diffTime = (curTime - lastUpdate)
                    lastUpdate = curTime

                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

                    if (speed > shakeThreshold) {
                        shakeCount++
                        if (shakeCount >= targetShakes) {
                            onSuccess()
                        }
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    AlertDialog(
        onDismissRequest = { /* Prevent dismiss */ },
        title = { Text("Shake to Dismiss") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Shake your phone!")
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { shakeCount / targetShakes.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("${shakeCount} / $targetShakes")
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Back")
            }
        }
    )
}