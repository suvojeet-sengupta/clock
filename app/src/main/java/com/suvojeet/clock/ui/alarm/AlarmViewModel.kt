package com.suvojeet.clock.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suvojeet.clock.data.alarm.AlarmEntity
import com.suvojeet.clock.data.alarm.AlarmRepository
import com.suvojeet.clock.data.alarm.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.suvojeet.clock.ui.alarm.AlarmUiModel
import com.suvojeet.clock.util.TimeFormatter
import kotlinx.coroutines.flow.map

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    val allAlarms: StateFlow<List<AlarmUiModel>> = repository.allAlarms
        .map { alarms ->
            alarms.map { alarm ->
                AlarmUiModel(
                    id = alarm.id,
                    timeDisplay = TimeFormatter.formatTimeForUi(TimeFormatter.parseDbTime(alarm.time)),
                    label = alarm.label,
                    isEnabled = alarm.isEnabled,
                    daysDisplay = TimeFormatter.formatDaysOfWeek(alarm.daysOfWeek),
                    originalEntity = alarm
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val id = repository.insert(alarm)
            val newAlarm = alarm.copy(id = id.toInt())
            if (newAlarm.isEnabled) { // Schedule only if enabled
                scheduler.schedule(newAlarm)
            }
        }
    }

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.update(alarm)
            if (alarm.isEnabled) {
                scheduler.schedule(alarm)
            } else {
                scheduler.cancel(alarm)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.delete(alarm)
            scheduler.cancel(alarm)
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        val newAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
        updateAlarm(newAlarm)
    }
}