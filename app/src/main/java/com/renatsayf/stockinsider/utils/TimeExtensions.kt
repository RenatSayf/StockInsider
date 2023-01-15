package com.renatsayf.stockinsider.utils

import android.icu.text.SimpleDateFormat
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