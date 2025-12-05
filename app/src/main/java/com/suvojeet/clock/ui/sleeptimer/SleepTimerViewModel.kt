package com.suvojeet.clock.ui.sleeptimer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SleepTimerUiState(
    val isPlaying: Boolean = false,
    val remainingTimeMs: Long = 0L,
    val selectedDuration: Int = 30, // minutes
    val selectedFadeDuration: Int = 30, // seconds
    val selectedSound: String = "Default"
)

@HiltViewModel
class SleepTimerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SleepTimerUiState())
    val uiState: StateFlow<SleepTimerUiState> = _uiState.asStateFlow()

    val durationOptions = listOf(15, 30, 45, 60, 90, 120)
    val fadeOptions = listOf(15, 30, 45, 60)
    val soundOptions = listOf("Default", "Notification", "Alarm", "Ringtone")

    fun setDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(selectedDuration = minutes)
    }

    fun setFadeDuration(seconds: Int) {
        _uiState.value = _uiState.value.copy(selectedFadeDuration = seconds)
    }

    fun setSound(sound: String) {
        _uiState.value = _uiState.value.copy(selectedSound = sound)
    }

    fun updatePlayingState(isPlaying: Boolean, remainingMs: Long) {
        _uiState.value = _uiState.value.copy(
            isPlaying = isPlaying,
            remainingTimeMs = remainingMs
        )
    }
}
