package com.example.util

import com.example.data.UmuConstants
import java.text.SimpleDateFormat
import java.util.*

data class TimeWindowStatus(
    val isOpen: Boolean,
    val currentFormattedTime: String,
    val currentFormattedDate: String,
    val currentDayOfWeek: String, // "Mon", "Tue", etc.
    val isWeekend: Boolean,
    val statusMessage: String,
    val isTestOverrideActive: Boolean
)

object TimeHelper {

    fun getCurrentDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return sdf.format(Date())
    }

    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
        return sdf.format(Date())
    }

    fun getCurrentDayOfWeek(): String {
        val sdf = SimpleDateFormat("EEE", Locale.ENGLISH)
        return sdf.format(Date()) // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    }

    fun getCurrentTimeFormatted(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        return sdf.format(Date())
    }

    /**
     * Checks if current time is strictly between 09:00 AM and 04:29 PM (09:00 to 16:29).
     */
    fun checkAttendanceTimeWindow(isTestOverrideActive: Boolean = false): TimeWindowStatus {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val dayOfWeekInt = cal.get(Calendar.DAY_OF_WEEK)
        val isWeekend = (dayOfWeekInt == Calendar.SATURDAY || dayOfWeekInt == Calendar.SUNDAY)

        val totalMinutes = hour * 60 + minute
        val windowStartMinutes = UmuConstants.WINDOW_START_HOUR * 60 + UmuConstants.WINDOW_START_MINUTE // 9 * 60 + 0 = 540
        val windowEndMinutes = UmuConstants.WINDOW_END_HOUR * 60 + UmuConstants.WINDOW_END_MINUTE     // 16 * 60 + 29 = 989

        val isWithinOfficialHours = (totalMinutes in windowStartMinutes..windowEndMinutes) && !isWeekend

        val isOpen = if (isTestOverrideActive) true else isWithinOfficialHours

        val statusMessage = when {
            isTestOverrideActive -> "Test Time Override Active (Attendance Enabled) 🛠️"
            isWeekend -> "Weekend / Off Day (Window Closed) 🏖️"
            isWithinOfficialHours -> "Window Open: 09:00 AM - 04:29 PM (Active) 🟢"
            totalMinutes < windowStartMinutes -> "Window Opens at 09:00 AM today ⏳"
            else -> "Window Closed (Was open till 04:29 PM) 🔒"
        }

        return TimeWindowStatus(
            isOpen = isOpen,
            currentFormattedTime = getCurrentTimeFormatted(),
            currentFormattedDate = getFormattedDate(),
            currentDayOfWeek = getCurrentDayOfWeek(),
            isWeekend = isWeekend,
            statusMessage = statusMessage,
            isTestOverrideActive = isTestOverrideActive
        )
    }
}
