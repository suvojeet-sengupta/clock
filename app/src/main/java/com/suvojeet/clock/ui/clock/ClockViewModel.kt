package com.suvojeet.clock.ui.clock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ClockViewModel : ViewModel() {

    private val _currentTime = MutableStateFlow(LocalTime.now())
    val currentTime: StateFlow<LocalTime> = _currentTime.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = LocalTime.now()
                delay(100) // Update every 100ms for smooth second hand
            }
        }
    }
}
