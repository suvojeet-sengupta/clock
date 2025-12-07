package com.suvojeet.clock.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.suvojeet.clock.util.TimeFormatter
import java.time.ZoneId
import java.time.ZonedDateTime

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
            putExtra("EXTRA_VIBRATE", alarm.isVibrateEnabled)
            putExtra("EXTRA_ALARM_ID", alarm.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val time = TimeFormatter.parseDbTime(alarm.time)
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var alarmTime = now.with(time).withSecond(0).withNano(0)

        if (alarm.daysOfWeek.isEmpty()) {
            // One-time alarm
            if (alarmTime.isBefore(now)) {
                alarmTime = alarmTime.plusDays(1)
            }
        } else {
            // Recurring alarm
            // daysOfWeek contains 1=Mon, ..., 7=Sun
            // ZonedDateTime.dayOfWeek returns DayOfWeek enum (MONDAY=1, SUNDAY=7)
            val currentDayOfWeek = now.dayOfWeek.value // 1..7

            // If today is in the list AND time is in future, schedule for today
            if (alarm.daysOfWeek.contains(currentDayOfWeek) && alarmTime.isAfter(now)) {
                 // alarmTime is already set to today/time, and it's in future. Keep it.
            } else {
                 // Find next day
                 var daysUntilNext = -1
                 for (i in 1..7) {
                     // Calculate next day index (1-7)
                     // (current + i - 1) % 7 + 1 handles the wrapping correctly
                     // e.g., current=7 (Sun), i=1 => (7+1-1)%7 + 1 = 0 + 1 = 1 (Mon)
                     val checkDay = (currentDayOfWeek + i - 1) % 7 + 1
                     if (alarm.daysOfWeek.contains(checkDay)) {
                         daysUntilNext = i
                         break
                     }
                 }
                 
                 if (daysUntilNext != -1) {
                     alarmTime = now.plusDays(daysUntilNext.toLong()).with(time).withSecond(0).withNano(0)
                 } else {
                     // Fallback just in case
                     if (alarmTime.isBefore(now)) alarmTime = alarmTime.plusDays(1)
                 }
            }
        }
        
        Log.d("AlarmScheduler", "Scheduling alarm for ${alarmTime.toLocalDateTime()}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.toEpochSecond() * 1000,
                    pendingIntent
                )
            } else {
                Log.w("AlarmScheduler", "Cannot schedule exact alarm, falling back to inexact")
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