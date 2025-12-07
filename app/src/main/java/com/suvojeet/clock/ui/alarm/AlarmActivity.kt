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
import androidx.compose.ui.res.stringResource
import com.suvojeet.clock.R

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

import androidx.activity.viewModels

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {
    
    // Injecting ViewModel
    private val viewModel: AlarmViewModel by viewModels()

    @Inject
    lateinit var settingsRepository: com.suvojeet.clock.data.settings.SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        turnScreenOnAndKeyguard()
        
        val label = intent.getStringExtra("EXTRA_MESSAGE") ?: getString(R.string.alarm_default_label)
        val soundUri = intent.getStringExtra("EXTRA_SOUND_URI")
        val vibratorEnabled = intent.getBooleanExtra("EXTRA_VIBRATE", true)
        val initialSnoozeCount = intent.getIntExtra("EXTRA_SNOOZE_COUNT", 0)
        
        // Initialize logic in ViewModel
        viewModel.initializeAlarm(label, soundUri, vibratorEnabled, initialSnoozeCount)

        lifecycleScope.launch {
            val dismissMethod = settingsRepository.dismissMethod.first()
            val mathDifficulty = settingsRepository.mathDifficulty.first()
            val snoozeDuration = settingsRepository.snoozeDuration.first()
            
            setContent {
                CosmicTheme {
                    val uiState by viewModel.uiState.collectAsState()
                    
                    AlarmTriggerScreen(
                        label = uiState.label,
                        currentTime = uiState.currentTime,
                        dismissMethod = dismissMethod,
                        mathDifficulty = mathDifficulty,
                        snoozeDurationMinutes = snoozeDuration,
                        canSnooze = uiState.canSnooze,
                        onDismiss = {
                            viewModel.stopAlarm()
                            cancelNotification()
                            finish()
                        },
                        onSnooze = {
                            viewModel.snooze()
                            // Note: uiState.snoozeCount is already updated by viewModel.snooze() in the VM
                            // But we are in a lambda capturing uiState? No, uiState is observed outside.
                            // Wait, if I call snooze(), the state updates asynchronously. 
                            // The uiState variable inside the lambda might be stale if captured.
                            // Actually, I should pass the current count + 1 or trust that I can get it.
                            // Better: let ViewModel handle the snoozing entirely? No, AlarmManager needs Activity context.
                            // I will simply pass `uiState.snoozeCount + 1` or just rely on the fact that if I snoozed, count is old + 1.
                            // Let's pass `uiState.snoozeCount + 1` explicitly as the new count to persist.
                            // OR, just read uiState.snoozeCount since it will be updated? No, recomposition takes time.
                            // Safest: uiState.snoozeCount + 1 (since we just verified canSnooze).
                            snoozeAlarm(label, soundUri, snoozeDuration, uiState.snoozeCount + 1)
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

    private fun cancelNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1) // NOTIFICATION_ID
    }

    private fun snoozeAlarm(label: String, soundUri: String?, snoozeDurationMinutes: Int, snoozeCount: Int) {
        cancelNotification()

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", label)
            putExtra("EXTRA_SOUND_URI", soundUri)
            putExtra("EXTRA_SNOOZE_COUNT", snoozeCount)
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
}

@Composable
fun AlarmTriggerScreen(
    label: String,
    currentTime: LocalTime,
    dismissMethod: DismissMethod,
    mathDifficulty: MathDifficulty,
    snoozeDurationMinutes: Int,
    canSnooze: Boolean,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    var showMathDialog by remember { mutableStateOf(false) }
    var showShakeScreen by remember { mutableStateOf(false) }

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
            .background(MaterialTheme.colorScheme.background), // Theme background
        contentAlignment = Alignment.Center
    ) {
        // Background pulsing circle
        Box(
            modifier = Modifier
                .size(250.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) // Theme primary
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
                if (canSnooze) {
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
                        Text(stringResource(R.string.snooze_button, snoozeDurationMinutes), style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                     Button(
                        onClick = { /* Do nothing */ },
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(stringResource(R.string.snooze_limit_reached), style = MaterialTheme.typography.titleMedium)
                    }
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
                    Text(stringResource(R.string.dismiss_button), style = MaterialTheme.typography.titleMedium)
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

// ... MathChallengeDialog and ShakeChallengeScreen same as before ... 
// (Wait, I need to include them or they get deleted because I'm replacing until line 550)
// I will include them to be safe.

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
        title = { Text(stringResource(R.string.solve_to_dismiss)) },
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
                    label = { Text(stringResource(R.string.answer_label)) },
                    isError = error,
                    singleLine = true
                )
                if (error) {
                    Text(
                        text = stringResource(R.string.incorrect_answer),
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
                Text(stringResource(R.string.submit_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.back_button))
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
        title = { Text(stringResource(R.string.shake_to_dismiss)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.shake_instruction))
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { shakeCount / targetShakes.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.shake_progress, shakeCount, targetShakes))
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