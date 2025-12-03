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
    private val scheduler: AlarmScheduler
) : ViewModel() {

    val allAlarms: StateFlow<List<AlarmEntity>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val id = repository.insert(alarm)
            // insert returns Long (rowId), but we need the ID from entity which is auto-generated.
            // Actually Room's insert returns rowId. If ID is auto-generated, we might need to fetch it or assume it matches if we reload.
            // For simplicity, let's assume we can schedule it. But wait, we need the ID for PendingIntent.
            // Let's create a copy with the ID if possible, or just schedule.
            // A better way is to insert, then the Flow updates, and we schedule based on that? 
            // Or just use the ID if we can get it.
            // Let's assume for now we schedule using the alarm object.
            // Note: If ID is 0, PendingIntent might conflict if we have multiple 0s before DB assigns ID.
            // Ideally we should get the ID back.
            // Let's update repository to return Long, and we use that as ID.
            scheduler.schedule(alarm.copy(id = id.toInt()))
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


