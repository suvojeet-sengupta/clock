package com.suvojeet.clock.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeFormatter {
    private val dbTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val uiTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

    fun parseDbTime(timeString: String): LocalTime {
        return try {
            LocalTime.parse(timeString, dbTimeFormatter)
        } catch (e: Exception) {
            try {
                // Fallback for potentially legacy data
                LocalTime.parse(timeString, DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
            } catch (e2: Exception) {
                LocalTime.now()
            }
        }
    }

    fun formatTimeForUi(time: LocalTime): String {
        return time.format(uiTimeFormatter)
    }

    fun formatTimeForDb(time: LocalTime): String {
        return time.format(dbTimeFormatter)
    }

    fun formatDaysOfWeek(days: List<Int>): String {
        if (days.isEmpty()) return "Never"
        if (days.size == 7) return "Every day"

        val dayNames = mapOf(
            1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun"
        )
        
        // Ensure sorted 1..7 (Mon..Sun)
        val sortedDays = days.sorted()
        
        // Check for "Weekdays" (Mon-Fri)
        if (sortedDays == listOf(1, 2, 3, 4, 5)) return "Weekdays"
        // Check for "Weekends" (Sat, Sun)
        if (sortedDays == listOf(6, 7)) return "Weekends"

        return sortedDays.joinToString(", ") { dayNames[it] ?: "" }
    }
}
