package com.renatsayf.stockinsider.utils

import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import kotlin.math.abs


@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = 31)
class AppCalendarTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule() //выполнение всех операций архитектурных компонентов в главном потоке

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {
    }

    @Test
    fun getCurrentTime() {

        val ekbZoneId = "Asia/Yekaterinburg"
        val chicagoZoneId = "America/Chicago"
        val washingtonZoneId = "America/Washington"

        val newYorkZoneId = "America/New_York"
        val newYorkTimeZone = TimeZone.getTimeZone(newYorkZoneId)
        val newYorkZoneOffset = newYorkTimeZone.rawOffset
        val newYorkCalendar = Calendar.getInstance(newYorkTimeZone).apply {
            timeInMillis = System.currentTimeMillis() + newYorkZoneOffset
        }
        val expectedTime = newYorkCalendar.timeInMillis.timeToFormattedString()
        val actualTime = AppCalendar.getCurrentTime().timeToFormattedString()
        Assert.assertEquals(expectedTime, actualTime)
    }

    @Test
    fun isWeekEnd() {

        val newYorkZoneId = "America/New_York"
        val newYorkTimeZone = TimeZone.getTimeZone(newYorkZoneId)
        val calendar = Calendar.getInstance(newYorkTimeZone).apply {
            set(Calendar.YEAR, 2022)
            set(Calendar.MONTH, 11)
            set(Calendar.DAY_OF_MONTH, 24)
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
        }
        AppCalendar.setCalendar(calendar)
        val currentTimeStr = AppCalendar.getCurrentTime().timeToFormattedString()
        var actualResult = AppCalendar.isWeekEnd
        Assert.assertEquals(true, actualResult)

        calendar.set(Calendar.DAY_OF_MONTH, 26)
        AppCalendar.setCalendar(calendar)
        actualResult = AppCalendar.isWeekEnd
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun isFillingTime() {
        val newYorkZoneId = "America/New_York"
        val newYorkTimeZone = TimeZone.getTimeZone(newYorkZoneId)
        val calendar = Calendar.getInstance(newYorkTimeZone).apply {
            set(Calendar.YEAR, 2022)
            set(Calendar.MONTH, 11)
            set(Calendar.DAY_OF_MONTH, 26)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
        }
        AppCalendar.setCalendar(calendar)
        val currentTimeStr = AppCalendar.getCurrentTime().timeToFormattedString()
        var actualResult = AppCalendar.isFillingTime
        Assert.assertEquals(true, actualResult)

        calendar.set(Calendar.HOUR_OF_DAY, 3)
        AppCalendar.setCalendar(calendar)
        actualResult = AppCalendar.isFillingTime
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun getNextFillingTime_on_the_weekend() {

        val newYorkZoneId = "America/New_York"
        val newYorkTimeZone = TimeZone.getTimeZone(newYorkZoneId)
        val calendar = Calendar.getInstance(newYorkTimeZone).apply {
            set(Calendar.YEAR, 2022)
            set(Calendar.MONTH, 11)
            set(Calendar.DAY_OF_MONTH, 24)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            timeInMillis += newYorkTimeZone.rawOffset
        }
        AppCalendar.setCalendar(calendar)
        val currentTimeStr = AppCalendar.getCurrentTime().timeToFormattedString()

        var isWeekEnd = AppCalendar.isWeekEnd
        Assert.assertEquals(true, isWeekEnd)

        var isFillingTime = AppCalendar.isFillingTime
        Assert.assertEquals(false, isFillingTime)

        val nextFillingTime = AppCalendar.getNextFillingTime(4)
        val nextFillingTimeStr = nextFillingTime.timeToFormattedString()

        calendar.timeInMillis = nextFillingTime
        AppCalendar.setCalendar(calendar)

        isWeekEnd = AppCalendar.isWeekEnd
        Assert.assertEquals(false, isWeekEnd)

        isFillingTime = AppCalendar.isFillingTime
        Assert.assertEquals(true, isFillingTime)

        val nextFillingTime1 = AppCalendar.getNextFillingTime(4)
        calendar.timeInMillis = nextFillingTime1
        AppCalendar.setCalendar(calendar)

        isWeekEnd = AppCalendar.isWeekEnd
        Assert.assertEquals(false, isWeekEnd)

        isFillingTime = AppCalendar.isFillingTime
        Assert.assertEquals(true, isFillingTime)

    }

    @Test
    fun getNextFillingTime_on_the_same_day() {
        val newYorkZoneId = "America/New_York"
        val newYorkTimeZone = TimeZone.getTimeZone(newYorkZoneId)
        val calendar = Calendar.getInstance(newYorkTimeZone).apply {
            set(Calendar.YEAR, 2022)
            set(Calendar.MONTH, 11)
            set(Calendar.DAY_OF_MONTH, 26)
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            timeInMillis += newYorkTimeZone.rawOffset
        }
        AppCalendar.setCalendar(calendar)
        val currentTimeStr = AppCalendar.getCurrentTime().timeToFormattedString()

        val actualTime = AppCalendar.getNextFillingTime(4).timeToFormattedString()
        Assert.assertEquals("2022-12-26T11:00:00", actualTime)
    }

    @Test
    fun getNextFillingTimeByDefaultTimeZone() {

        val currentNewYorkTime = AppCalendar.getCurrentTime().timeToFormattedString()

        val ekbZoneId = "Asia/Yekaterinburg"
        val defaultZone = TimeZone.getTimeZone(ekbZoneId)
        val defaultCalendar = Calendar.getInstance(defaultZone).apply {
            set(Calendar.YEAR, 2022)
            set(Calendar.MONTH, 11)
            set(Calendar.DAY_OF_MONTH, 26)
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            timeInMillis += defaultZone.rawOffset
        }
        val defaultCurrentTime = defaultCalendar.timeInMillis.timeToFormattedString()

        val hourOffset = 1
        val nextTimeNewYork = AppCalendar.getNextFillingTime(hourOffset)
        val nextTimeDefault = AppCalendar.getNextFillingTimeByDefaultTimeZone(hourOffset)

        val diffInMillis = abs(nextTimeNewYork - nextTimeDefault)
        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val actualResult = diffInHours - hourOffset

        val defaultHourOffset = TimeUnit.MILLISECONDS.toHours(TimeZone.getDefault().rawOffset.toLong())
        val appHourOffset = TimeUnit.MILLISECONDS.toHours(AppCalendar.timeZone.rawOffset.toLong())
        val expectedResult = defaultHourOffset + appHourOffset

        currentNewYorkTime
    }
}