package com.renatsayf.stockinsider.utils

import android.icu.util.Calendar
import android.icu.util.TimeZone
import javax.inject.Inject

class AppCalendar @Inject constructor(private val timeZone: TimeZone)
{
    companion object
    {
        const val START_HOUR : Int = 5
        const val START_MINUTE : Int = 0
        const val END_HOUR : Int = 23
        private const val END_MINUTE : Int = 0
        private const val LOAD_INTERVAL : Long = 60 * 5 * 1000
    }

    private val isCheckWeekend = true

    private data class Result(val isFilingTime : Boolean, val isAfterFiling : Boolean)

    fun getNextCalendar() : Calendar
    {
        val calendar = Calendar.getInstance(timeZone)
        if (calendar.isWeekend && isCheckWeekend)
        {
            return calendar.apply {
                var day = 1
                while (this.isWeekend)
                {
                    this.add(Calendar.DATE, day)
                    this.set(Calendar.HOUR_OF_DAY, START_HOUR)
                    this.set(Calendar.MINUTE, START_MINUTE)
                    day++
                }
            }
        }
        if (checkFilingTime().isFilingTime)
        {
            return calendar.apply {
                this.timeInMillis += LOAD_INTERVAL
            }
        }
        if (!checkFilingTime().isFilingTime && checkFilingTime().isAfterFiling)
        {
            return calendar.apply {
                this.add(Calendar.DATE, 1)
                this.set(Calendar.HOUR_OF_DAY, START_HOUR)
                this.set(Calendar.MINUTE, START_MINUTE)
            }
        }
        if (!checkFilingTime().isFilingTime && !checkFilingTime().isAfterFiling)
        {
            return calendar.apply {
                this.set(Calendar.HOUR_OF_DAY, START_HOUR)
                this.set(Calendar.MINUTE, START_MINUTE)
            }
        }
        return calendar
    }

    private fun checkFilingTime() : Result
    {
        val startCalendar = Calendar.getInstance(timeZone).apply {
            set(Calendar.HOUR_OF_DAY, START_HOUR)
            set(Calendar.MINUTE, START_MINUTE)
        }
        val endCalendar = Calendar.getInstance(timeZone).apply {
            set(Calendar.HOUR_OF_DAY, END_HOUR)
            set(Calendar.MINUTE, END_MINUTE)
        }
        val currentCalendar = Calendar.getInstance(timeZone)
        val isFilingTime = currentCalendar.after(startCalendar) && currentCalendar.before(endCalendar)

        val isAfterFiling: Boolean
        isAfterFiling = currentCalendar.after(endCalendar)

        return Result(isFilingTime, isAfterFiling)
    }
}