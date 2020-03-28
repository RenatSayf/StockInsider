package com.renatsayf.stockinsider.network

import android.content.Context
import android.icu.util.Calendar
import android.icu.util.TimeZone
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.service.GetFilingDataTask
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.utils.Utils
import com.renatsayf.stockinsider.utils.IsFilingTime
import java.util.*
import javax.inject.Inject


class Scheduler @Inject constructor()
{
    companion object
    {
        val TAG : String = this.hashCode().toString()
        const val KEY_DEAL_LIST : String = "key_deal_list"
    }

    @Inject
    lateinit var notification : ServiceNotification

    @Inject
    lateinit var utils: Utils

    init
    {
        MainActivity.appComponent.inject(this)
    }

    fun setNetSchedule(context : Context, isNextDay : Boolean) : Timer
    {
        val timer = Timer()
        val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
        val chicagoCalendar = Calendar.getInstance(timeZone)
        if (IsFilingTime.checking(timeZone).isFilingTime)
        {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis() + IsFilingTime.LOAD_INTERVAL
            }
            timer.schedule(GetFilingDataTask(context, timer), calendar.time, IsFilingTime.LOAD_INTERVAL)
            val startTime = utils.getFormattedDateTime(0, calendar.time)
            val message = "Расписание установлено в $startTime. \n" +
                    "Каждый час мы буде проверять новые сделки"
            notification.createNotification(context, null, message, R.drawable.ic_stock_hause_cold, R.color.colorGold).show()
            println(message)
        }
        else
        {
            val tomorrowCalendar = Calendar.getInstance(timeZone).apply {
                timeInMillis = System.currentTimeMillis() + 60000 * 60 * 24
            }
            val nextDay = tomorrowCalendar[Calendar.DAY_OF_MONTH]
            println("nextDay = $nextDay")
            val todayCalendar = Calendar.getInstance(timeZone)
            todayCalendar.apply {
                timeInMillis = System.currentTimeMillis()
                when (isNextDay)
                {
                    true -> set(Calendar.DAY_OF_MONTH, tomorrowCalendar[Calendar.DAY_OF_MONTH])
                    else -> set(Calendar.DAY_OF_MONTH, this[Calendar.DAY_OF_MONTH])
                }
                set(Calendar.HOUR_OF_DAY, IsFilingTime.START_HOUR)
                set(Calendar.MINUTE, IsFilingTime.START_MINUTE)
            }
            timer.schedule(GetFilingDataTask(context, timer), tomorrowCalendar.time, IsFilingTime.LOAD_INTERVAL)
            val startTime = utils.getFormattedDateTime(0, tomorrowCalendar.time)
            val message = "Расписание установлено в - $startTime (вр.мест.)\n" +
                    "Текущее время Чикаго - ${chicagoCalendar[Calendar.HOUR_OF_DAY]} : ${chicagoCalendar[Calendar.MINUTE]}"
            notification.createNotification(context, null, message, R.drawable.ic_stock_hause_cold, R.color.colorGold).show()
            println(message)
        }
        return timer
    }

    fun cancelNetSchedule(context: Context, timer: Timer)
    {
        timer.cancel()
        timer.purge()
        println("Расписание отменено...")
        notification.createNotification(context, null, "Расписание отменено...", R.drawable.ic_stock_hause_cold, R.color.colorGold).show()
    }



}