package com.renatsayf.stockinsider.utils

import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.test.espresso.core.internal.deps.guava.base.Joiner.on
import com.renatsayf.stockinsider.R
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mockito.mock
import java.util.*

class AppCalendarTest
{
    private var timeZone : TimeZone? = TimeZone.getTimeZone("America/Chicago")
    private var expectedCalendar : Calendar? = null
    private var actualCalendar : Calendar? = null


    @Before
    fun setUp()
    {
        expectedCalendar = Calendar.getInstance(timeZone).apply {
            set(Calendar.DAY_OF_MONTH, 6)
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 0)
        }
    }

    @After
    fun tearDown()
    {
        timeZone = null
        expectedCalendar = null
        actualCalendar = null
    }

    @Test
    fun getNextCalendar()
    {
        actualCalendar = timeZone?.let {
            AppCalendar(it).getNextCalendar()
        }
        val message = "******  actualCalendar data = ${actualCalendar?.time} ****  expectedCalendar data = ${expectedCalendar?.time}  ***"
        assertEquals(message, expectedCalendar?.get(Calendar.DAY_OF_MONTH), actualCalendar?.get(Calendar.DAY_OF_MONTH))
        assertEquals(message, expectedCalendar?.get(Calendar.HOUR_OF_DAY), actualCalendar?.get(Calendar.HOUR_OF_DAY))
        assertEquals(message, expectedCalendar?.get(Calendar.MINUTE), actualCalendar?.get(Calendar.MINUTE))
        println(message)
    }
}