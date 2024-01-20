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

    private var _calendar = Calendar.getInstance()
    private var _withWeekend = true
    private var _withFillingTime = true

    init {
        val defaultOffset = TimeZone.getDefault().rawOffset
        val newYorkOffset = timeZoneNY.rawOffset
        _calendar.apply {
            timeInMillis = initTime - defaultOffset + newYorkOffset
        }
    }

    fun getCurrentTime(): Long {
        return _calendar.timeInMillis
    }

    val isWeekEnd: Boolean
        get() {
            _calendar.timeInMillis.timeToFormattedString()
            return _calendar.isWeekend
        }

    val isFillingTime: Boolean
        get() {
            val currentHour = _calendar.get(Calendar.HOUR_OF_DAY)
            return currentHour in (START_FILLING_HOUR + 1)..END_FILLING_HOUR && !_calendar.isWeekend
        }

    fun getNextFillingTime(periodInMinute: Long): Long {

        val nextTime = _calendar.timeInMillis + (TimeUnit.MINUTES.toMillis(periodInMinute))
        "**************** workerPeriod: $periodInMinute *******************".printIfDebug()
        "**************** nextTime: ${nextTime.timeToFormattedString()} *******************".printIfDebug()

        val newCalendar = _calendar
        newCalendar.timeInMillis = nextTime

        if (_withWeekend) {
            while (isWeekEnd) {
                newCalendar.timeInMillis += TimeUnit.HOURS.toMillis(1L)
                val postWeekendTime = newCalendar.timeInMillis.timeToFormattedString()
                "**************** postWeekendTime: $postWeekendTime ***********************".printIfDebug()
            }
        }
        if (_withFillingTime) {
            while (!isFillingTime) {
                newCalendar.timeInMillis += TimeUnit.HOURS.toMillis(1L)
                val nextFillingTime = newCalendar.timeInMillis.timeToFormattedString()
                "******************* nextFillingTime: $nextFillingTime *********************".printIfDebug()
            }
        }
        "******************* newCalendar.time (America/New_York): ${newCalendar.timeInMillis.timeToFormattedString()} *********************".printIfDebug()
        return newCalendar.timeInMillis
    }

    fun getNextFillingTimeByDefaultTimeZone(periodInMinute: Long): Long {
        _withWeekend = true
        _withFillingTime = true
        val utcOffset = timeZoneNY.rawOffset
        val defaultOffset = TimeZone.getDefault().rawOffset
        return getNextFillingTime(periodInMinute) - utcOffset + defaultOffset
    }

    fun getNextTestTimeByDefaultTimeZone(periodInMinute: Long): Long {
        _withWeekend = false
        _withFillingTime = false
        val utcOffset = timeZoneNY.rawOffset
        val defaultOffset = TimeZone.getDefault().rawOffset
        return getNextFillingTime(periodInMinute) - utcOffset + defaultOffset
    }

}

fun AppCalendar.getNextStartTime(
    periodInMinute: Long = FireBaseConfig.trackingPeriod,
    isTestMode: Boolean = Constants.TEST_MODE
): Long {

    return if (isTestMode) {
        this.getNextTestTimeByDefaultTimeZone(periodInMinute)
    }
    else this.getNextFillingTimeByDefaultTimeZone(periodInMinute)
}






















