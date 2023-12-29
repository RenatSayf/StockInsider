package com.renatsayf.stockinsider.utils

import android.icu.text.SimpleDateFormat
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.models.DateFormats
import java.util.*

fun Date.getFormattedDateTime(mode: Int): String {
    val time: Date = this

    val patternDateTime = "dd.MM.yyyy  HH:mm"
    val patternDate = "dd.MM.yyyy"
    val patternTime = "HH:MM"

    when (mode) {
        0 -> {
            val dateFormat = SimpleDateFormat(patternDateTime, Locale.getDefault())
            return dateFormat.format(time)
        }
        1 -> {
            val dateFormat = SimpleDateFormat(patternDate, Locale.getDefault())
            return dateFormat.format(time)
        }
        2 -> {
            val dateFormat = SimpleDateFormat(patternTime, Locale.getDefault())
            return dateFormat.format(time)
        }
        else -> {
            val dateFormat = SimpleDateFormat(patternDateTime, Locale.getDefault())
            return dateFormat.format(time)
        }
    }
}

fun String.formattedStringToLong(locale: Locale = Locale.US): Long {

    val dateFormat = SimpleDateFormat(DateFormats.DEFAULT_FORMAT.format, locale)
    return try {
        dateFormat.parse(this).time
    } catch (e: Exception) {
        if (BuildConfig.DEBUG) e.printStackTrace()
        -1
    }
}

fun Long.timeToFormattedStringWithoutSeconds(): String =
    SimpleDateFormat(DateFormats.DATE_TIME_YY_MM_FORMAT.format, Locale.getDefault())
        .format(this).replace("T", " ")

fun getTimeOffsetIfWeekEnd(
    daysAgo: Int,
    startTime: Long = System.currentTimeMillis()
): Int {

    val millisInDay = 86_400_000L
    var daysShift = daysAgo
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York")).apply {
        timeInMillis = startTime
    }
    val todayInMillis = calendar.timeInMillis
    var daysAgoInMillis = todayInMillis - daysShift * millisInDay
    calendar.timeInMillis = daysAgoInMillis
    var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    while(dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
        daysShift++
        daysAgoInMillis -= millisInDay
        calendar.timeInMillis = daysAgoInMillis
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    }
    return daysShift
}



















