package com.renatsayf.stockinsider.utils

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.widget.CheckBox
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.DailyTime
import java.util.*

class Utils
{
    fun getFilingOrTradeValue(context : Context, position : Int) : String
    {
        val filingValue : String
        return try
        {
            val filingValues = context.resources?.getIntArray(R.array.value_for_filing_date)
            filingValue = filingValues?.get(position).toString()
            filingValue
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            ""
        }
    }

    fun convertBoolToString(value : Boolean) : String
    {
        return when
        {
            value -> "1"
            else -> ""
        }
    }

    fun getGroupingValue(context : Context, position : Int) : String
    {
        val groupingValue : String
        return try
        {
            val groupingValues = context.resources?.getIntArray(R.array.value_for_grouping)
            groupingValue = groupingValues?.get(position).toString()
            groupingValue
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            ""
        }
    }

    fun getSortingValue(context : Context, position : Int) : String
    {
        val sortingValue : String
        return try
        {
            val sortingValues = context.resources?.getIntArray(R.array.value_for_sorting)
            sortingValue = sortingValues?.get(position).toString()
            sortingValue
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            ""
        }
    }

    fun isOfficerToString(value : Boolean) : String
    {
        return when
        {
            value -> "1&iscob=1&isceo=1&ispres=1&iscoo=1&iscfo=1&isgc=1&isvp=1"
            else -> ""
        }
    }

    fun getCheckBoxValue(checkBox : CheckBox) : String
    {
        val id = checkBox.id
        return if(checkBox.isChecked && id == R.id.officer_CheBox)
        {
            "1&iscob=1&isceo=1&ispres=1&iscoo=1&iscfo=1&isgc=1&isvp=1"
        }
        else if (checkBox.isChecked && id != R.id.officer_CheBox)
        {
            "1"
        }
        else
        {
            ""
        }
    }

    fun chicagoTime(context : Context) : DailyTime
    {
        val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
        val calendar = Calendar.getInstance(timeZone)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        return DailyTime(hour, minute, second)
    }

    fun getFormattedDateTime(mode: Int, date: Date?): String
    {
        val calendar = GregorianCalendar.getInstance()
        val time: Date?
        time = date ?: calendar.time

        val patternDateTime = "dd.MM.yyyy  HH:mm"
        val patternDate = "dd.MM.yyyy"
        val patternTime = "HH:MM"

        when (mode)
        {
            0 ->
            {
                val dateFormat = SimpleDateFormat(patternDateTime, Locale.getDefault())
                return dateFormat.format(time)
            }
            1 ->
            {
                val dateFormat = SimpleDateFormat(patternDate, Locale.getDefault())
                return dateFormat.format(time)
            }
            2 ->
            {
                val dateFormat = SimpleDateFormat(patternTime, Locale.getDefault())
                return dateFormat.format(time)
            }
            else ->
            {
                val dateFormat = SimpleDateFormat(patternDateTime, Locale.getDefault())
                return dateFormat.format(time)
            }
        }
    }

}