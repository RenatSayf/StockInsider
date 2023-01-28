package com.renatsayf.stockinsider.utils

import android.icu.util.Calendar
import android.icu.util.TimeZone
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.ui.settings.Constants
import java.util.concurrent.TimeUnit

class AppCalendar(
    private val initTime: Long = System.currentTimeMillis()
) {

    companion object {
        private const val START_FILLING_HOUR: Int = 6
        private const val END_FILLING_HOUR: Int = 23
    }

    val timeZoneNY: TimeZone
        get() {
            return TimeZone.getTimeZone("America/New_York")
        }

    private var calendar = Calendar.getInstance()
    private var withWeekend = true
    private var withFillingTime = true

    init {
        val defaultOffset = TimeZone.getDefault().rawOffset
        val newYorkOffset = timeZoneNY.rawOffset
        calendar.apply {
            timeInMillis = initTime - defaultOffset + newYorkOffset
        }
    }

    fun getCurrentTime(): Long {
        return calendar.timeInMillis
    }

    val isWeekEnd: Boolean
        get() {
            calendar.timeInMillis.timeToFormattedString()
            return calendar.isWeekend
        }

    val isFillingTime: Boolean
        get() {
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            return currentHour in (START_FILLING_HOUR + 1)..END_FILLING_HOUR && !calendar.isWeekend
        }

    fun getNextFillingTime(periodInMinute: Long): Long {

        val nextTime = calendar.timeInMillis + (TimeUnit.MINUTES.toMillis(periodInMinute))
        println("**************** workerPeriod: $periodInMinute *******************")
        println("**************** nextTime: ${nextTime.timeToFormattedString()} *******************")

        val newCalendar = calendar
        newCalendar.timeInMillis = nextTime

        if (withWeekend) {
            while (isWeekEnd) {
                newCalendar.timeInMillis += TimeUnit.HOURS.toMillis(1L)
                val postWeekendTime = newCalendar.timeInMillis.timeToFormattedString()
                println("**************** postWeekendTime: $postWeekendTime ***********************")
            }
        }
        if (withFillingTime) {
            while (!isFillingTime) {
                newCalendar.timeInMillis += TimeUnit.HOURS.toMillis(1L)
                val nextFillingTime = newCalendar.timeInMillis.timeToFormattedString()
                println("******************* nextFillingTime: $nextFillingTime *********************")
            }
        }
        println("******************* newCalendar.time (America/New_York): ${newCalendar.timeInMillis.timeToFormattedString()} *********************")
        return newCalendar.timeInMillis
    }

    fun getNextFillingTimeByDefaultTimeZone(periodInMinute: Long): Long {
        withWeekend = true
        withFillingTime = true
        val utcOffset = timeZoneNY.rawOffset
        val defaultOffset = TimeZone.getDefault().rawOffset
        return getNextFillingTime(periodInMinute) - utcOffset + defaultOffset
    }

    fun getNextTestTimeByDefaultTimeZone(periodInMinute: Long): Long {
        withWeekend = false
        withFillingTime = false
        val utcOffset = timeZoneNY.rawOffset
        val defaultOffset = TimeZone.getDefault().rawOffset
        return getNextFillingTime(periodInMinute) - utcOffset + defaultOffset
    }

}

fun AppCalendar.getNextStartTime(
    periodInMinute: Long = 0L,
    isTestMode: Boolean = Constants.TEST_MODE
): Long {

    return if (isTestMode && periodInMinute == 0L) {
        this.getNextTestTimeByDefaultTimeZone(
            periodInMinute = FireBaseConfig.workerPeriod
        )
    }
    else if (isTestMode && periodInMinute > 0L) {
        this.getNextTestTimeByDefaultTimeZone(periodInMinute)
    }
    else this.getNextFillingTimeByDefaultTimeZone(
        periodInMinute = FireBaseConfig.workerPeriod,
    )
}






















