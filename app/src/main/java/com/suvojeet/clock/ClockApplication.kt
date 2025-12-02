package com.suvojeet.clock

import android.app.Application
import androidx.room.Room
import com.suvojeet.clock.data.alarm.AlarmDatabase

class ClockApplication : Application() {
    lateinit var database: AlarmDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AlarmDatabase::class.java,
            "clock_database"
        ).build()
    }
}
