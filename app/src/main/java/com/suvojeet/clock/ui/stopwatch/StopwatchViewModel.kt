package com.suvojeet.clock.ui.stopwatch

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StopwatchViewModel @Inject constructor() : ViewModel() {

    private val _elapsedTime = MutableStateFlow(0L) // in milliseconds
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // Each lap entry contains: cumulative time, split time (time since last lap)
    data class LapData(val cumulativeTime: Long, val splitTime: Long)
    
    private val _laps = MutableStateFlow<List<LapData>>(emptyList())
    val laps: StateFlow<List<LapData>> = _laps.asStateFlow()

    private var stopwatchJob: Job? = null
    private var startTime = 0L
    private var lastLapTime = 0L

    fun startStopwatch() {
        if (!_isRunning.value) {
            _isRunning.value = true
            startTime = System.currentTimeMillis() - _elapsedTime.value
            stopwatchJob = viewModelScope.launch {
                while (_isRunning.value) {
                    _elapsedTime.value = System.currentTimeMillis() - startTime
                    delay(30) // Update frequency ~30fps
                }
            }
        }
    }

    fun pauseStopwatch() {
        _isRunning.value = false
        stopwatchJob?.cancel()
    }

    fun resetStopwatch() {
        pauseStopwatch()
        _elapsedTime.value = 0L
        _laps.value = emptyList()
        lastLapTime = 0L
    }

    fun lap() {
        if (_isRunning.value) {
            val currentTime = _elapsedTime.value
            val splitTime = currentTime - lastLapTime
            lastLapTime = currentTime
            
            val currentLaps = _laps.value.toMutableList()
            currentLaps.add(0, LapData(currentTime, splitTime)) // Add to top
            _laps.value = currentLaps
        }
    }
    
    /**
     * Format lap times as a shareable string
     */
    fun formatLapsForShare(): String {
        val lapsData = _laps.value
        if (lapsData.isEmpty()) return "No laps recorded"
        
        val sb = StringBuilder()
        sb.appendLine("🏃 Stopwatch Lap Times")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        
        lapsData.reversed().forEachIndexed { index, lap ->
            val lapNum = index + 1
            val cumTime = formatTimeForShare(lap.cumulativeTime)
            val splitTime = formatTimeForShare(lap.splitTime)
            sb.appendLine("Lap $lapNum: $cumTime (+$splitTime)")
        }
        
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("Total: ${formatTimeForShare(_elapsedTime.value)}")
        
        return sb.toString()
    }
    
    private fun formatTimeForShare(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        val hundredths = (millis % 1000) / 10
        return String.format(Locale.getDefault(), "%02d:%02d.%02d", minutes, seconds, hundredths)
    }
    
    /**
     * Create a share intent for lap times.
     * The composable should handle starting the activity.
     * @return Intent for sharing lap times
     */
    fun createShareIntent(): Intent {
        val shareText = formatLapsForShare()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        return Intent.createChooser(sendIntent, "Share Lap Times")
    }
}
