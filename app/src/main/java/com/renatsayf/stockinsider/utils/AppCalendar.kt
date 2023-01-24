package com.renatsayf.stockinsider.utils

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.SystemClock
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
    private var withFillingTime = true

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun setCalendar(calendar: Calendar) {
        this.calendar = calendar
    }

    fun getCurrentTime(): Long {
        return System.currentTimeMillis() + timeZone.rawOffset
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

    private fun checkFillingTime(calendar: Calendar): Boolean {
        val hourOffset = TimeUnit.MILLISECONDS.toHours(timeZone.rawOffset.toLong()).toInt()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) - hourOffset
        return currentHour in (START_FILLING_HOUR + 1)..END_FILLING_HOUR && !calendar.isWeekend
    }

    fun getNextFillingTime(workerPeriod: Long): Long {
        val string = getCurrentTime().timeToFormattedString()
        val string1 = calendar.timeInMillis.timeToFormattedString()
        val nextTime = getCurrentTime() + (TimeUnit.MINUTES.toMillis(workerPeriod))
        println("**************** workerPeriod: $workerPeriod *******************")
        println("**************** nextTime: ${nextTime.timeToFormattedString()} *******************")
        val newCalendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = nextTime
        }
        if (withWeekend) {
            while (newCalendar.isWeekend) {
                newCalendar.timeInMillis += TimeUnit.HOURS.toMillis(1L)
                val postWeekendTime = newCalendar.timeInMillis.timeToFormattedString()
                println("**************** postWeekendTime: $postWeekendTime ***********************")
            }
        }
        if (withFillingTime) {
            while (!checkFillingTime(newCalendar)) {
                newCalendar.timeInMillis += TimeUnit.HOURS.toMillis(1L)
                val nextFillingTime = newCalendar.timeInMillis.timeToFormattedString()
                println("******************* nextFillingTime: $nextFillingTime *********************")
            }
        }
        println("******************* newCalendar.time (UTC): ${newCalendar.timeInMillis.timeToFormattedString()} *********************")
        return newCalendar.timeInMillis
    }

    fun getNextFillingTimeByUTCTimeZone(workerPeriod: Long): Long {
        withWeekend = true
        withFillingTime = true
        val utcOffset = timeZone.rawOffset
        return getNextFillingTime(workerPeriod) - utcOffset
    }

    fun getNextTestTimeByUTCTimeZone(workerPeriod: Long): Long {
        withWeekend = false
        withFillingTime = false
        val utcOffset = timeZone.rawOffset
        return getNextFillingTime(workerPeriod) - utcOffset
    }

}






















