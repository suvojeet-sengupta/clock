package com.suvojeet.clock

import android.app.Application
import androidx.room.Room
import com.suvojeet.clock.data.alarm.AlarmDatabase
import com.suvojeet.clock.data.settings.SettingsRepository

class ClockApplication : Application() {
    lateinit var database: AlarmDatabase
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AlarmDatabase::class.java,
            "clock_database"
        )
        .fallbackToDestructiveMigration()
        .build()
        
        settingsRepository = SettingsRepository(applicationContext)
    }
}
