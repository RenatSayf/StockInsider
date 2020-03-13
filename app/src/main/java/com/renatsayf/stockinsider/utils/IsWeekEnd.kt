package com.renatsayf.stockinsider.utils

import android.icu.util.Calendar
import android.icu.util.TimeZone
import java.util.*

object IsWeekEnd
{
    fun checking(date : Date, timeZone : TimeZone) : Boolean
    {
        val calendar = Calendar.getInstance().apply {
            this.timeZone = timeZone
            timeInMillis = date.time
        }
//        println(timeZone.displayName)
//        println(calendar[Calendar.HOUR_OF_DAY])
        println("is weekend - ${calendar.isWeekend}")
        return calendar.isWeekend
    }
}