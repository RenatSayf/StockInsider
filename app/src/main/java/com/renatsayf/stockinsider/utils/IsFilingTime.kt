package com.renatsayf.stockinsider.utils

import android.app.AlarmManager
import android.icu.util.Calendar
import android.icu.util.TimeZone

object IsFilingTime
{
    var START_HOUR : Int = 5
    var START_MINUTE : Int = 0
    var END_HOUR : Int = 23
    var END_MINUTE : Int = 0
    var START_DELAY = 900000L
    var LOAD_INTERVAL : Long = 1800000L

    data class Result(val isFilingTime : Boolean, val isAfterFiling : Boolean)

//    init
//    {
//        val zone = "America/Chicago"
//        val timeZone = TimeZone.getTimeZone(zone)
//        val calendar = Calendar.getInstance(timeZone)
//        calendar.apply {
//            timeInMillis += 60000L
//        }
//        START_HOUR = calendar[Calendar.HOUR_OF_DAY]
//        START_MINUTE = calendar[Calendar.MINUTE]
//
//        calendar.apply {
//            timeInMillis += 60000L * 10
//        }
//        END_HOUR = calendar[Calendar.HOUR_OF_DAY]
//        END_MINUTE = calendar[Calendar.MINUTE]
//
//        START_DELAY = 5000L
//        LOAD_INTERVAL = 120000L
//    }

    fun checking(timeZone : TimeZone) : Result
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

        println("Start time - ${startCalendar.get(Calendar.HOUR_OF_DAY)} : ${startCalendar.get(Calendar.MINUTE)}")
        println("End time - ${endCalendar.get(Calendar.HOUR_OF_DAY)} : ${endCalendar.get(Calendar.MINUTE)}")
        println("Current Chicago time - ${currentCalendar.get(Calendar.HOUR_OF_DAY)} : ${currentCalendar.get(Calendar.MINUTE)}")
        println("isFilingTime = $isFilingTime")
        println("isAfterFiling = $isAfterFiling")
        return Result(isFilingTime, isAfterFiling)
    }
}