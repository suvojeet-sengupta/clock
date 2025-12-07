package com.suvojeet.clock.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.suvojeet.clock.MainActivity
import com.suvojeet.clock.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SleepTimerService : Service() {

    private val binder = SleepTimerBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _remainingTime = MutableStateFlow(0L) // in milliseconds
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var totalDuration: Long = 0L
    private var fadeDuration: Long = 30_000L // 30 seconds fade by default
    private var selectedSoundName: String = "Default"

    companion object {
        const val CHANNEL_ID = "sleep_timer_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_STOP = "com.suvojeet.clock.STOP_SLEEP_TIMER"

        // Sound options - using system ringtones as fallback
        val SOUND_OPTIONS = listOf(
            "Default",
            "Notification",
            "Alarm",
            "Ringtone"
        )
    }

    inner class SleepTimerBinder : Binder() {
        fun getService(): SleepTimerService = this@SleepTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sleep timer running"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SleepTimer::WakeLock"
        ).apply {
            acquire(2 * 60 * 60 * 1000L) // 2 hours max
        }
    }

    fun startTimer(durationMinutes: Int, fadeSeconds: Int, soundName: String) {
        totalDuration = durationMinutes * 60 * 1000L
        fadeDuration = fadeSeconds * 1000L
        selectedSoundName = soundName

        _remainingTime.value = totalDuration
        _isPlaying.value = true

        startForeground(NOTIFICATION_ID, createNotification())
        playSound(soundName)
        startCountdown()
    }

    private fun getSoundUri(soundName: String): Uri {
        return when (soundName) {
            "Notification" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            "Alarm" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            "Ringtone" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    private fun playSound(soundName: String) {
        try {
            mediaPlayer?.release()
            val uri = getSoundUri(soundName)
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                setVolume(0.5f, 0.5f) // Start at 50% for sleep sounds
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If fails, try default notification sound
            try {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, defaultUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    isLooping = true
                    setVolume(0.5f, 0.5f)
                    prepare()
                    start()
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_remainingTime.value > 0 && isActive) {
                delay(1000)
                _remainingTime.value = _remainingTime.value - 1000

                // Start fading volume when remaining time <= fadeDuration
                if (_remainingTime.value <= fadeDuration && fadeDuration > 0) {
                     // Calculate fade progress (1.0 -> 0.0) based on remaining time
                    val fadeProgress = (_remainingTime.value.toFloat() / fadeDuration).coerceIn(0f, 1f)
                    val volume = fadeProgress * 0.5f // Max volume is 0.5f
                    try {
                        mediaPlayer?.setVolume(volume, volume)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Update notification every 30 seconds
                if (_remainingTime.value % 30_000 == 0L) {
                    updateNotification()
                }
            }

            // Timer finished
            stopTimer()
        }
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, SleepTimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remainingMinutes = (_remainingTime.value / 60_000).toInt()
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sleep Timer")
            .setContentText("$remainingMinutes min remaining")
            .setSmallIcon(R.drawable.ic_bedtime)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    fun stopTimer() {
        timerJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _remainingTime.value = 0
        try {
            wakeLock?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        try {
            mediaPlayer?.release()
            wakeLock?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        serviceScope.cancel()
    }
}
