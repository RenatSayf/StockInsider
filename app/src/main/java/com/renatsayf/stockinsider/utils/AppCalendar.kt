package com.renatsayf.stockinsider.utils

import android.icu.util.Calendar
import android.icu.util.TimeZone

class AppCalendar constructor(private val timeZone: TimeZone)
{
    private var startHour : Int = 1
    private var startMinute : Int = 0
    private var endHour : Int = 23
    private var endMinute : Int = 0
    private var loadInterval : Long = 60 * 15 * 1000

    private val isCheckWeekend = true

    private data class Result(val isFilingTime : Boolean, val isAfterFiling : Boolean)

    fun getNextCalendar() : Calendar
    {
        val calendar = Calendar.getInstance(timeZone)
        if (calendar.isWeekend && isCheckWeekend)
        {
            return calendar.apply {
                var amount = 1
                while (this.isWeekend)
                {
                    this.add(Calendar.DATE, amount)
                    this.set(Calendar.HOUR_OF_DAY, startHour)
                    this.set(Calendar.MINUTE, startMinute)
                    amount++
                }
            }
        }
        if (isFilingTime().isFilingTime)
        {
            return calendar.apply {
                this.timeInMillis += loadInterval
            }
        }
        if (!isFilingTime().isFilingTime && isFilingTime().isAfterFiling)
        {
            return calendar.apply {
                this.add(Calendar.DATE, 1)
                this.set(Calendar.HOUR_OF_DAY, startHour)
                this.set(Calendar.MINUTE, startMinute)
            }
        }
        if (!isFilingTime().isFilingTime && !isFilingTime().isAfterFiling)
        {
            return calendar.apply {
                this.set(Calendar.HOUR_OF_DAY, startHour)
                this.set(Calendar.MINUTE, startMinute)
            }
        }
        return calendar
    }

    private fun isFilingTime() : Result
    {
        val startCalendar = Calendar.getInstance(timeZone).apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
        }
        val endCalendar = Calendar.getInstance(timeZone).apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMinute)
        }
        val currentCalendar = Calendar.getInstance(timeZone)
        val isFilingTime = currentCalendar.after(startCalendar) && currentCalendar.before(endCalendar)

        val isAfterFiling: Boolean
        isAfterFiling = currentCalendar.after(endCalendar)

        return Result(isFilingTime, isAfterFiling)
    }
}