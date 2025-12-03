package com.suvojeet.clock.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor() : ViewModel() {

    private val _timeLeft = MutableStateFlow(0L) // in milliseconds
    val timeLeft: StateFlow<Long> = _timeLeft.asStateFlow()

    private val _totalTime = MutableStateFlow(0L)
    val totalTime: StateFlow<Long> = _totalTime.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var timerJob: Job? = null

    fun setTimer(hours: Int, minutes: Int, seconds: Int) {
        val duration = (hours * 3600 + minutes * 60 + seconds) * 1000L
        _totalTime.value = duration
        _timeLeft.value = duration
    }

    fun startTimer() {
        if (_timeLeft.value > 0 && !_isRunning.value) {
            _isRunning.value = true
            timerJob = viewModelScope.launch {
                val startTime = System.currentTimeMillis()
                val initialTimeLeft = _timeLeft.value
                
                while (_timeLeft.value > 0) {
                    val elapsed = System.currentTimeMillis() - startTime
                    _timeLeft.value = (initialTimeLeft - elapsed).coerceAtLeast(0)
                    delay(50) // Update frequency
                }
                _isRunning.value = false
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timeLeft.value = _totalTime.value
    }
}
