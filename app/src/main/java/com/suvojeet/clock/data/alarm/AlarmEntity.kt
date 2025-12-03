package com.suvojeet.clock.data.alarm

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val time: String, // Stored as "HH:mm"
    val label: String = "",
    val isEnabled: Boolean = true,
    val isVibrateEnabled: Boolean = true,
    val daysOfWeek: List<Int> = emptyList(), // 1=Monday, 7=Sunday
    val soundUri: String = "" // URI string for the selected ringtone
)
