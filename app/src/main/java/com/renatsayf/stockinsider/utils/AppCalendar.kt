package com.renatsayf.stockinsider.utils

import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.annotation.VisibleForTesting
import java.util.concurrent.TimeUnit

object AppCalendar {

    private const val START_FILLING_HOUR: Int = 6
    private const val END_FILLING_HOUR: Int = 22

    val timeZone: TimeZone
        get() {
            return TimeZone.getTimeZone("America/New_York")
        }

    private var calendar = Calendar.getInstance(timeZone).apply {
        timeInMillis = System.currentTimeMillis() + timeZone.rawOffset
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun setCalendar(calendar: Calendar) {
        this.calendar = calendar
    }

    fun getCurrentTime(): Long {
        return calendar.timeInMillis
    }

    val isWeekEnd: Boolean
        get() {
            return calendar.isWeekend
        }

    val isFillingTime: Boolean
        get() {
            val formattedString = calendar.timeInMillis.timeToFormattedString()
            val hourOffset = TimeUnit.MILLISECONDS.toHours(timeZone.rawOffset.toLong()).toInt()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY) - hourOffset
            return currentHour in (START_FILLING_HOUR + 1)..END_FILLING_HOUR && !calendar.isWeekend
        }

    fun getNextFillingTime(offsetHour: Int): Long {
        val nextTime = calendar.timeInMillis + (TimeUnit.HOURS.toMillis(offsetHour.toLong()))
        val newCalendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = nextTime
        }
        while (newCalendar.isWeekend) {
            newCalendar.timeInMillis += TimeUnit.HOURS.toMillis(1L)
        }
        setCalendar(newCalendar)
        while (!this.isFillingTime) {
            newCalendar.timeInMillis += TimeUnit.HOURS.toMillis(1L)
        }

        return this.calendar.timeInMillis
    }

    fun getNextFillingTimeByDefaultTimeZone(offsetHour: Int): Long {
        val utcOffset = timeZone.rawOffset
        val id = TimeZone.getDefault().id
        val defaultOffset = TimeZone.getDefault().rawOffset
        return getNextFillingTime(offsetHour) - utcOffset + defaultOffset
    }
}