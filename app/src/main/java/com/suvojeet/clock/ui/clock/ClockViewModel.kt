package com.suvojeet.clock.ui.clock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.suvojeet.clock.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.map

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _currentTime = MutableStateFlow(LocalTime.now())
    val currentTime: StateFlow<LocalTime> = _currentTime.asStateFlow()

    val is24HourFormat: StateFlow<Boolean> = settingsRepository.is24HourFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val clockStyle: StateFlow<ClockStyle> = settingsRepository.clockStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClockStyle.CLASSIC)

    fun updateClockStyle(style: ClockStyle) {
        viewModelScope.launch {
            settingsRepository.setClockStyle(style)
        }
    }

    val selectedWorldClocks: StateFlow<List<WorldClockData>> = settingsRepository.selectedWorldClockZones
        .map { zoneIds ->
            zoneIds.map { zoneId ->
                val zone = java.time.ZoneId.of(zoneId)
                val now = java.time.ZonedDateTime.now(zone)
                WorldClockData(
                    zoneId = zoneId,
                    city = zoneId.split("/").last().replace("_", " "),
                    time = now.toLocalTime(),
                    offset = zone.rules.getOffset(java.time.Instant.now()).id.replace("Z", "+00:00")
                )
            }.sortedBy { it.city }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableZones: List<String> = java.time.ZoneId.getAvailableZoneIds().sorted()

    fun addWorldClock(zoneId: String) {
        viewModelScope.launch {
            settingsRepository.addWorldClockZone(zoneId)
        }
    }

    fun removeWorldClock(zoneId: String) {
        viewModelScope.launch {
            settingsRepository.removeWorldClockZone(zoneId)
        }
    }

    init {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = LocalTime.now()
                delay(100) // Update every 100ms for smooth second hand
            }
        }
    }
}

data class WorldClockData(
    val zoneId: String,
    val city: String,
    val time: LocalTime,
    val offset: String
)


