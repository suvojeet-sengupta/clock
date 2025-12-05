package com.suvojeet.clock.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.suvojeet.clock.data.alarm.AlarmEntity
import com.suvojeet.clock.data.alarm.AlarmRepository
import com.suvojeet.clock.data.alarm.AlarmScheduler
import com.suvojeet.clock.data.alarm.AndroidAlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val alexaRepository: com.suvojeet.clock.data.alexa.AlexaRepository
) : ViewModel() {

    val allAlarms: StateFlow<List<AlarmEntity>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val id = repository.insert(alarm)
            scheduler.schedule(alarm.copy(id = id.toInt()))
            
            // Trigger Alexa Reminder
            // We need to parse the time string "HH:mm" to millis
            try {
                val parts = alarm.time.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                
                val now = java.time.LocalDateTime.now()
                var targetTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
                if (targetTime.isBefore(now)) {
                    targetTime = targetTime.plusDays(1)
                }
                val timeInMillis = targetTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                alexaRepository.createReminder(alarm.label, timeInMillis)
            } catch (e: Exception) {
                // Ignore parsing errors
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


