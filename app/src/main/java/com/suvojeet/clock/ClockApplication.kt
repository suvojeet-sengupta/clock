package com.suvojeet.clock

import android.app.Application
import androidx.room.Room
import com.suvojeet.clock.data.alarm.AlarmDatabase
import com.suvojeet.clock.data.settings.SettingsRepository

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClockApplication : Application()
