package com.renatsayf.stockinsider.utils

import android.app.AlarmManager
import android.icu.util.Calendar
import android.icu.util.TimeZone

object IsFilingTime
{
    const val START_HOUR : Int = 5
    const val START_MINUTE : Int = 0
    const val END_HOUR : Int = 21
    const val END_MINUTE : Int = 0
    const val INTERVAL : Long = AlarmManager.INTERVAL_HOUR

    fun checking(timeZone : TimeZone) : Boolean
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
        val isStockTime = currentCalendar.after(startCalendar) && currentCalendar.before(endCalendar)
        println("Start time - ${startCalendar.get(Calendar.HOUR_OF_DAY)} : ${startCalendar.get(Calendar.MINUTE)}, timeInMillis = ${startCalendar.timeInMillis}")
        println("End time - ${endCalendar.get(Calendar.HOUR_OF_DAY)} : ${endCalendar.get(Calendar.MINUTE)}, timeInMillis = ${endCalendar.timeInMillis}")
        println("Current Chicago time - ${currentCalendar.get(Calendar.HOUR_OF_DAY)} : ${currentCalendar.get(Calendar.MINUTE)}, timeInMillis = ${currentCalendar.timeInMillis}")
        println("isFilingTime = $isStockTime")
        return isStockTime
    }
}