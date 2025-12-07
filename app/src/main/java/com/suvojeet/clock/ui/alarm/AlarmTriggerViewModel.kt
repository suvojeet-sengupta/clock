package com.suvojeet.clock.ui.alarm

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suvojeet.clock.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class AlarmTriggerUiState(
    val isRinging: Boolean = false,
    val label: String = "Alarm",
    val currentTime: LocalTime = LocalTime.now(),
    val snoozeCount: Int = 0,
    val maxSnoozeCount: Int = 0,
    val canSnooze: Boolean = true
)

@HiltViewModel
class AlarmTriggerViewModel @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmTriggerUiState())
    val uiState: StateFlow<AlarmTriggerUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var volumeJob: Job? = null
    private var timeUpdateJob: Job? = null
    private var soundUri: Uri? = null

    init {
        startTimeUpdate()
        viewModelScope.launch {
            val maxSnooze = settingsRepository.maxSnoozeCount.first()
            _uiState.value = _uiState.value.copy(maxSnoozeCount = maxSnooze)
        }
    }

    private fun startTimeUpdate() {
        timeUpdateJob = viewModelScope.launch {
            while (true) {
                _uiState.value = _uiState.value.copy(currentTime = LocalTime.now())
                delay(1000)
            }
        }
    }

    fun initializeAlarm(label: String, soundUrl: String?, vibratorEnabled: Boolean, initialSnoozeCount: Int = 0) {
        _uiState.value = _uiState.value.copy(
            label = label, 
            isRinging = true,
            snoozeCount = initialSnoozeCount
        )
        
        soundUri = if (!soundUrl.isNullOrEmpty()) {
            Uri.parse(soundUrl)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        viewModelScope.launch {
            val gradualVolume = settingsRepository.gradualVolume.first()
            startRinging(soundUri!!, gradualVolume, vibratorEnabled)
        }
    }

    private fun startRinging(uri: Uri, gradualVolume: Boolean, vibratorEnabled: Boolean) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(application, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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
            // Fallback to ringtone manager if media player fails
            val ringtone = RingtoneManager.getRingtone(application, uri)
            ringtone?.play()
        }

        if (vibratorEnabled) {
            startVibration()
        }
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 1000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun startGradualVolumeIncrease() {
        volumeJob?.cancel()
        volumeJob = viewModelScope.launch {
            for (i in 1..10) {
                delay(3000)
                val volume = 0.1f + (i * 0.09f)
                mediaPlayer?.setVolume(volume, volume)
            }
        }
    }

    fun stopAlarm() {
        _uiState.value = _uiState.value.copy(isRinging = false)
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

    fun snooze() {
        val currentSnooze = _uiState.value.snoozeCount
        val maxSnooze = _uiState.value.maxSnoozeCount
        
        // Logical check: 0 means unlimited
        if (maxSnooze == 0 || currentSnooze < maxSnooze) {
            _uiState.value = _uiState.value.copy(snoozeCount = currentSnooze + 1)
            stopAlarm()
            updateSnoozeState()
        }
    }
    
    // Check if snooze is still possible for UI updates
    private fun updateSnoozeState() {
         val currentSnooze = _uiState.value.snoozeCount
         val maxSnooze = _uiState.value.maxSnoozeCount
         val canSnooze = maxSnooze == 0 || currentSnooze < maxSnooze
         _uiState.value = _uiState.value.copy(canSnooze = canSnooze)
    }

    override fun onCleared() {
        super.onCleared()
        stopAlarm()
        timeUpdateJob?.cancel()
    }
}
