package com.suvojeet.clock.ui.clock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.suvojeet.clock.data.alarm.AlarmEntity
import com.suvojeet.clock.data.alarm.AlarmRepository
import com.suvojeet.clock.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Represents the next upcoming alarm with calculated time information
 */
data class NextAlarmInfo(
    val alarm: AlarmEntity,
    val nextTriggerTime: LocalDateTime,
    val timeUntilAlarm: String
)

@HiltViewModel
class ClockViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private val _currentTime = MutableStateFlow(LocalTime.now())
    val currentTime: StateFlow<LocalTime> = _currentTime.asStateFlow()

    val is24HourFormat: StateFlow<Boolean> = settingsRepository.is24HourFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val clockStyle: StateFlow<ClockStyle> = settingsRepository.clockStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClockStyle.CLASSIC)
    
    /**
     * Calculates and provides the next upcoming alarm
     */
    val nextAlarm: StateFlow<NextAlarmInfo?> = alarmRepository.enabledAlarms
        .map { alarms -> calculateNextAlarm(alarms) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun calculateNextAlarm(alarms: List<AlarmEntity>): NextAlarmInfo? {
        if (alarms.isEmpty()) return null
        
        val now = LocalDateTime.now()
        
        return alarms.mapNotNull { alarm ->
            try {
                val alarmTime = LocalTime.parse(alarm.time, DateTimeFormatter.ofPattern("HH:mm"))
                var nextTrigger = now.with(alarmTime)
                
                // If the alarm time has passed today, schedule for tomorrow
                if (nextTrigger.isBefore(now)) {
                    nextTrigger = nextTrigger.plusDays(1)
                }
                
                // If alarm has specific days, find the next matching day
                if (alarm.daysOfWeek.isNotEmpty()) {
                    // Java's DayOfWeek: Monday=1, Tuesday=2, ..., Sunday=7
                    // This matches our stored format in AlarmEntity
                    val currentDayOfWeek = now.dayOfWeek.value
                    var daysToAdd = 0
                    var found = false
                    
                    // Check up to 8 days ahead (today + 7 more days) to find next matching day
                    // The formula ((currentDayOfWeek - 1 + i) % 7) + 1 cycles through days:
                    // Example: If today is Wednesday (3):
                    //   i=0: ((3-1+0) % 7) + 1 = 3 (Wed)
                    //   i=1: ((3-1+1) % 7) + 1 = 4 (Thu)
                    //   ...
                    //   i=7: ((3-1+7) % 7) + 1 = 3 (next Wed)
                    for (i in 0..7) {
                        val checkDay = ((currentDayOfWeek - 1 + i) % 7) + 1
                        if (alarm.daysOfWeek.contains(checkDay)) {
                            if (i == 0 && nextTrigger.isBefore(now)) {
                                // Today's alarm already passed, try next occurrence
                                continue
                            }
                            daysToAdd = i
                            found = true
                            break
                        }
                    }
                    
                    if (!found) return@mapNotNull null
                    nextTrigger = now.with(alarmTime).plusDays(daysToAdd.toLong())
                    if (nextTrigger.isBefore(now)) {
                        nextTrigger = nextTrigger.plusDays(7)
                    }
                }
                
                NextAlarmInfo(
                    alarm = alarm,
                    nextTriggerTime = nextTrigger,
                    timeUntilAlarm = formatTimeUntil(now, nextTrigger)
                )
            } catch (e: Exception) {
                null
            }
        }.minByOrNull { it.nextTriggerTime }
    }

    private fun formatTimeUntil(from: LocalDateTime, to: LocalDateTime): String {
        val duration = java.time.Duration.between(from, to)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        
        return when {
            hours > 24 -> {
                val days = hours / 24
                if (days == 1L) "in 1 day" else "in $days days"
            }
            hours > 0 -> {
                if (minutes > 0) "in ${hours}h ${minutes}m" else "in ${hours}h"
            }
            minutes > 0 -> "in ${minutes}m"
            else -> "now"
        }
    }

    fun updateClockStyle(style: ClockStyle) {
        viewModelScope.launch {
            settingsRepository.setClockStyle(style)
        }
    }

    val selectedWorldClocks: StateFlow<List<WorldClockData>> = settingsRepository.selectedWorldClockZones
        .map { zoneIds ->
            val localZone = java.time.ZoneId.systemDefault()
            val localOffset = localZone.rules.getOffset(java.time.Instant.now())
            
            zoneIds.map { zoneId ->
                val zone = java.time.ZoneId.of(zoneId)
                val now = java.time.ZonedDateTime.now(zone)
                val zoneOffset = zone.rules.getOffset(java.time.Instant.now())
                val diffSeconds = zoneOffset.totalSeconds - localOffset.totalSeconds
                val diffHours = diffSeconds / 3600
                
                WorldClockData(
                    zoneId = zoneId,
                    city = zoneId.split("/").last().replace("_", " "),
                    time = now.toLocalTime(),
                    offset = zone.rules.getOffset(java.time.Instant.now()).id.replace("Z", "+00:00"),
                    timeDifferenceHours = diffHours
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

    private val _searchResults = MutableStateFlow<List<WorldClockData>>(emptyList())
    val searchResults: StateFlow<List<WorldClockData>> = _searchResults.asStateFlow()

    fun searchZones(query: String) {
        viewModelScope.launch {
            val filtered = if (query.isEmpty()) {
                // Suggest some popular cities if query is empty
                listOf("America/New_York", "Europe/London", "Asia/Tokyo", "Australia/Sydney", "Europe/Paris")
            } else {
                availableZones.filter { it.contains(query, ignoreCase = true) }
            }

            _searchResults.value = filtered.map { zoneId ->
                val zone = java.time.ZoneId.of(zoneId)
                val now = java.time.ZonedDateTime.now(zone)
                WorldClockData(
                    zoneId = zoneId,
                    city = zoneId.split("/").last().replace("_", " "),
                    time = now.toLocalTime(),
                    offset = zone.rules.getOffset(java.time.Instant.now()).id.replace("Z", "+00:00")
                )
            }
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
    val offset: String,
    val timeDifferenceHours: Int = 0 // Difference from local time in hours
)
