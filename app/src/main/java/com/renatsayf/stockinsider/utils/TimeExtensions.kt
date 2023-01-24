package com.renatsayf.stockinsider.utils

import android.icu.text.DateFormat
import android.icu.text.DateFormatSymbols
import android.icu.text.SimpleDateFormat
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.models.DateFormats
import java.time.format.DateTimeFormatter
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