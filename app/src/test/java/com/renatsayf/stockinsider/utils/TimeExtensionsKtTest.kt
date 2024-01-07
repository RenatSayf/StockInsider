package com.renatsayf.stockinsider.utils

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class TimeExtensionsKtTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getTimeOffsetIfWeekEnd_in_monday() {

        val time = LocalDateTime.of(2023, 12, 25, 16, 59)
        val millis = time.toInstant(ZoneOffset.ofHours(5)).toEpochMilli()

        val input = 1
        val actualDays = getTimeOffsetIfWeekEnd(input, millis)
        val expectedDays = 4
        Assert.assertEquals(expectedDays, actualDays)
    }

    @Test
    fun getTimeOffsetIfWeekEnd_in_sunday() {

        val time = LocalDateTime.of(2023, 12, 24, 16, 59)
        val millis = time.toInstant(ZoneOffset.ofHours(5)).toEpochMilli()
        val input = 1
        val actualDays = getTimeOffsetIfWeekEnd(input, millis)
        val expectedDays = 3
        Assert.assertEquals(expectedDays, actualDays)
    }

    @Test
    fun getTimeOffsetIfWeekEnd_in_saturday() {

        val time = LocalDateTime.of(2023, 12, 23, 16, 59)
        val millis = time.toInstant(ZoneOffset.ofHours(5)).toEpochMilli()
        val input = 1
        val actualDays = getTimeOffsetIfWeekEnd(input, millis)
        val expectedDays = 2
        Assert.assertEquals(expectedDays, actualDays)
    }
}