package com.renatsayf.stockinsider.utils

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

    private val periodInMinute: Long = 60L

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getCurrentTime() {

        //val ekbZoneId = "Asia/Yekaterinburg"
        //val chicagoZoneId = "America/Chicago"
        //val washingtonZoneId = "America/Washington"

        val appCalendar = AppCalendar()
        val actualTime = appCalendar.getCurrentTime()
        val utcTime = System.currentTimeMillis() - TimeZone.getDefault().rawOffset

        val actualOffset = (actualTime - utcTime) / 3600 / 1000
        Assert.assertEquals(-5, actualOffset)
    }

    @Test
    fun isWeekEnd() {

        var inputTime = "2023-01-22T06:00:00".formattedStringToLong()

        var appCalendar = AppCalendar(inputTime)
        var actualResult = appCalendar.isWeekEnd
        Assert.assertEquals(true, actualResult)

        inputTime = "2023-01-24T06:00:00".formattedStringToLong()
        appCalendar = AppCalendar(inputTime)
        actualResult = appCalendar.isWeekEnd
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun isFillingTime() {

        var inputTime = "2023-01-26T17:00:00".formattedStringToLong()
        var appCalendar = AppCalendar(inputTime)
        var actualResult = appCalendar.isFillingTime
        Assert.assertEquals(true, actualResult)

        inputTime = "2023-01-26T10:00:00".formattedStringToLong()
        appCalendar = AppCalendar(inputTime)
        actualResult = appCalendar.isFillingTime
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun getNextFillingTime_on_the_weekend() {

        val inputTime = "2023-01-22T10:00:00".formattedStringToLong()

        val appCalendar = AppCalendar(inputTime)

        val isWeekEnd = appCalendar.isWeekEnd
        Assert.assertEquals(true, isWeekEnd)

        val isFillingTime = appCalendar.isFillingTime
        Assert.assertEquals(false, isFillingTime)

        val nextFillingTime = appCalendar.getNextFillingTimeByDefaultTimeZone(periodInMinute)
        val actualResult = nextFillingTime.timeToFormattedString()
        val expectedResult = "2023-01-23 17:00:00"
        Assert.assertEquals(expectedResult, actualResult)
    }

    @Test
    fun getNextFillingTime_on_real_time() {

        val workerPeriodInMinute: Long = 20

        var appCalendar = AppCalendar()

        val firstTime = appCalendar.getNextFillingTime(workerPeriodInMinute)
        println("****************** ${this.javaClass.simpleName}.getNextFillingTime_on_real_time(): ${firstTime.timeToFormattedString()} *****************")
        println("****************** delay $workerPeriodInMinute sec *******************************")
        
        Thread.sleep(workerPeriodInMinute * 1000)

        appCalendar = AppCalendar()
        val secondTime = appCalendar.getNextFillingTime(workerPeriodInMinute)
        println("****************** ${this.javaClass.simpleName}.getNextFillingTime_on_real_time(): ${secondTime.timeToFormattedString()} *****************")

        val actualResult = (secondTime - firstTime) / 1000
        Assert.assertTrue(actualResult in 19..22)
    }

    @Test
    fun getNextFillingTime_on_the_same_day() {

        val inputTime = "2023-01-04T07:00:00".formattedStringToLong()
        val appCalendar = AppCalendar(inputTime)

        val actualTime = appCalendar.getNextFillingTimeByDefaultTimeZone(periodInMinute).timeToFormattedString()
        Assert.assertEquals("2023-01-04 08:00:00", actualTime)
    }

    @Test
    fun getNextFillingTime_on_the_end_filling_day() {

        val inputTime = "2023-01-04T22:00:00".formattedStringToLong()
        val appCalendar = AppCalendar(inputTime)

        val actualTime = appCalendar.getNextFillingTimeByDefaultTimeZone(periodInMinute).timeToFormattedString()
        Assert.assertEquals("2023-01-04 23:00:00", actualTime)
    }

    @Test
    fun getNextFillingTimeByDefaultTimeZone() {

        val inputTime = "2023-01-04T07:00:00".formattedStringToLong()
        val appCalendar = AppCalendar(inputTime)

        val nextTimeNewYork = appCalendar.getNextFillingTime(periodInMinute)
        val nextTimeDefault = appCalendar.getNextFillingTimeByDefaultTimeZone(periodInMinute)

        val diffInMillis = abs(nextTimeNewYork - nextTimeDefault)
        val actualResult = TimeUnit.MILLISECONDS.toHours(diffInMillis) - TimeUnit.MINUTES.toHours(periodInMinute)

        val defaultHourOffset = TimeUnit.MILLISECONDS.toHours(TimeZone.getDefault().rawOffset.toLong())
        val appHourOffset = TimeUnit.MILLISECONDS.toHours(appCalendar.timeZoneNY.rawOffset.toLong())
        val expectedResult = abs(defaultHourOffset) + abs(appHourOffset)

        Assert.assertEquals(expectedResult, actualResult)
    }

}