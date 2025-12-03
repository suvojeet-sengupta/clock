package com.suvojeet.clock.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

interface AlarmScheduler {
    fun schedule(alarm: AlarmEntity)
    fun cancel(alarm: AlarmEntity)
}

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", alarm.label.ifEmpty { "Alarm" })
            putExtra("EXTRA_SOUND_URI", alarm.soundUri)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val time = try {
            LocalTime.parse(alarm.time)
        } catch (e: Exception) {
            try {
                LocalTime.parse(alarm.time, DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
            } catch (e2: Exception) {
                LocalTime.parse(alarm.time, DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
            }
        }
        
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var alarmTime = now.with(time)

        if (alarmTime.isBefore(now)) {
            alarmTime = alarmTime.plusDays(1)
        }

        // Handle days of week if needed, for now simple daily or one-time
        // If daysOfWeek is not empty, we need more complex logic to find next occurrence
        // For MVP/Expressive update, let's stick to simple scheduling
        
        Log.d("AlarmScheduler", "Scheduling alarm for ${alarmTime.toLocalDateTime()}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.toEpochSecond() * 1000,
                    pendingIntent
                )
            } else {
                // Request permission or fallback
                Log.w("AlarmScheduler", "Cannot schedule exact alarm")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.toEpochSecond() * 1000,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime.toEpochSecond() * 1000,
                pendingIntent
            )
        }
    }

    override fun cancel(alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
