package com.suvojeet.clock.ui.alarm

import com.suvojeet.clock.data.alarm.AlarmEntity

data class AlarmUiModel(
    val id: Int,
    val timeDisplay: String,
    val label: String,
    val isEnabled: Boolean,
    val daysDisplay: String,
    val originalEntity: AlarmEntity // Keep reference for edit operations/toggling
)
