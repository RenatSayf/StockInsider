package com.renatsayf.stockinsider.utils

import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.annotation.VisibleForTesting
import com.renatsayf.stockinsider.BuildConfig
import java.util.concurrent.TimeUnit

object AppCalendar {

    private const val START_FILLING_HOUR: Int = 6
    private const val END_FILLING_HOUR: Int = 23

    val timeZone: TimeZone
        get() {
            return TimeZone.getTimeZone("America/New_York")
        }

    private var calendar = Calendar.getInstance(timeZone).apply {
        timeInMillis = System.currentTimeMillis() + timeZone.rawOffset
    }
    private var withWeekend = true

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun setCalendar(calendar: Calendar) {
        this.calendar = calendar
    }

    fun getCurrentTime(): Long {
        return calendar.timeInMillis
    }

    val currentTimeByDefaultTimeZone: Long
        get() {
            val time = System.currentTimeMillis() + TimeZone.getDefault().rawOffset
            if (BuildConfig.DEBUG) println("************************ timeByDefaultTimeZone: ${time.timeToFormattedString()} ************************")
            return time
        }

    val isWeekEnd: Boolean
        get() {
            return calendar.isWeekend
        }

    val isFillingTime: Boolean
        get() {
            val hourOffset = TimeUnit.MILLISECONDS.toHours(timeZone.rawOffset.toLong()).toInt()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY) - hourOffset
            return currentHour in (START_FILLING_HOUR + 1)..END_FILLING_HOUR && !calendar.isWeekend
        }

    fun getNextFillingTime(workerPeriod: Long): Long {
        val nextTime = calendar.timeInMillis + (TimeUnit.MINUTES.toMillis(workerPeriod))
        val newCalendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = nextTime
        }
        if (withWeekend) {
            while (newCalendar.isWeekend) {
                newCalendar.timeInMillis += TimeUnit.HOURS.toMillis(1L)
            }
        }
        setCalendar(newCalendar)
        while (!this.isFillingTime) {
            newCalendar.timeInMillis += TimeUnit.HOURS.toMillis(1L)
        }

        return this.calendar.timeInMillis
    }

    fun getNextFillingTimeByDefaultTimeZone(workerPeriod: Long, withWeekend: Boolean = true): Long {
        this.withWeekend = withWeekend
        val utcOffset = timeZone.rawOffset
        val defaultOffset = TimeZone.getDefault().rawOffset
        return getNextFillingTime(workerPeriod) - utcOffset + defaultOffset
    }

}






















