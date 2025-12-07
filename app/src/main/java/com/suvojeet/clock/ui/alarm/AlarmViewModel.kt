package com.suvojeet.clock.ui.alarm

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suvojeet.clock.data.alarm.AlarmEntity
import com.suvojeet.clock.data.alarm.AlarmRepository
import com.suvojeet.clock.data.alarm.AlarmScheduler
import com.suvojeet.clock.data.alarm.AndroidAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val application: Application
) : ViewModel() {

    private val alarmScheduler: AlarmScheduler = AndroidAlarmScheduler(application)

    val allAlarms: StateFlow<List<AlarmEntity>> = repository.allAlarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val id = repository.insert(alarm)
            val newAlarm = alarm.copy(id = id.toInt())
            if (newAlarm.isEnabled) {
                alarmScheduler.schedule(newAlarm)
            }
        }
    }

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.update(alarm)
            if (alarm.isEnabled) {
                alarmScheduler.schedule(alarm)
            } else {
                alarmScheduler.cancel(alarm)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.delete(alarm)
            alarmScheduler.cancel(alarm)
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        val newAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
        updateAlarm(newAlarm)
    }
}
