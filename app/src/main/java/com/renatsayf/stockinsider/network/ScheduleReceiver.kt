package com.renatsayf.stockinsider.network

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.widget.Toast
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomDBProvider
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.ui.main.MainFragment
import com.renatsayf.stockinsider.utils.IsWeekEnd
import com.renatsayf.stockinsider.utils.Utils
import com.renatsayf.stockinsider.utils.IsFilingTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ScheduleReceiver @Inject constructor() : BroadcastReceiver(), SearchRequest.Companion.IBackWorkerListener
{
    companion object
    {
        val TAG : String = this.hashCode().toString()
        const val REQUEST_CODE : Int = 245455456
        const val KEY_DEAL_LIST : String = "key_deal_list"
    }

    @Inject
    lateinit var dbProvider : RoomDBProvider

    @Inject
    lateinit var utils : Utils

    @Inject
    lateinit var searchRequest : SearchRequest

    @Inject
    lateinit var notification : ServiceNotification

    private lateinit var db : AppDao
    private lateinit var context : Context

    init
    {
        App().component.inject(this)
    }

    override fun onReceive(context : Context?, intent : Intent?)
    {
        context?.let {
            val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
            val (isFilingTime, isAfterFiling) = IsFilingTime.checking(timeZone)
            if (!isFilingTime)
            {
                cancelNetSchedule(it, REQUEST_CODE)
                when(isAfterFiling)
                {
                    true -> setNetSchedule(it, REQUEST_CODE, true)
                    else -> setNetSchedule(it, REQUEST_CODE, false)
                }
                return
            }
            searchRequest.setBackWorkerListener(this)
            this.context = context
            db = dbProvider.getDao(it)
            val roomSearchSet = getSearchSetByName(db, MainFragment.DEFAULT_SET)
            val requestParams = SearchSet(MainFragment.DEFAULT_SET).apply {
                ticker = roomSearchSet.ticker
                filingPeriod = utils.getFilingOrTradeValue(context, roomSearchSet.filingPeriod)
                tradePeriod = utils.getFilingOrTradeValue(context, roomSearchSet.tradePeriod)
                isPurchase = utils.converBoolToString(roomSearchSet.isPurchase)
                isSale = utils.converBoolToString(roomSearchSet.isSale)
                tradedMin = roomSearchSet.tradedMin
                tradedMax = roomSearchSet.tradedMax
                isOfficer = utils.isOfficerToString(roomSearchSet.isOfficer)
                isDirector = utils.converBoolToString(roomSearchSet.isDirector)
                isTenPercent = utils.converBoolToString(roomSearchSet.isTenPercent)
                groupBy = utils.getGroupingValue(context, roomSearchSet.groupBy)
                sortBy = utils.getSortingValue(context, roomSearchSet.sortBy)
            }

            when(IsWeekEnd.checking(Date(System.currentTimeMillis()), timeZone))
            {
                false ->
                {
                    //searchRequest.getTradingScreen(TAG, requestParams)
                    println("Запрос отправлен...")
                }
            }
        }
        return
    }

    fun setNetSchedule(context : Context, requestCode : Int, isNextDay : Boolean) : PendingIntent
    {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, requestCode, intent, 0)
        }

        val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
        val chicagoCalendar = Calendar.getInstance(timeZone)
        if (IsFilingTime.checking(timeZone).isFilingTime)
        {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis() + IsFilingTime.INTERVAL
            }
            alarmManager.apply {
                setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, IsFilingTime.INTERVAL, alarmIntent)
                val startTime = "${calendar[Calendar.HOUR_OF_DAY]} : ${calendar[Calendar.MINUTE]}"
                val message = "Расписание установлено в $startTime. \n" +
                        "Каждый час мы буде проверять новые сделки"
                notification.notify(context, null, message, R.drawable.ic_stock_hause)
            }
        }
        else
        {
            val calendar = Calendar.getInstance(timeZone).apply {
                timeInMillis = System.currentTimeMillis()
                if (isNextDay)
                {
                    set(Calendar.DAY_OF_MONTH, this[Calendar.DAY_OF_MONTH] + 1)
                }
                set(Calendar.HOUR_OF_DAY, IsFilingTime.START_HOUR)
                set(Calendar.MINUTE, IsFilingTime.START_MINUTE)
            }

            alarmManager.apply {
                setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
                setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, IsFilingTime.INTERVAL, alarmIntent)
                val patternDateTime = "dd.MM.yyyy  HH:mm"
                val dateFormat = SimpleDateFormat(patternDateTime, Locale.getDefault())
                val startTime = dateFormat.format(calendar.timeInMillis)

                val message = "Расписание установлено в - $startTime (вр.мест.)\n" +
                        "Текущее время Чикаго - ${chicagoCalendar[Calendar.HOUR_OF_DAY]} : ${chicagoCalendar[Calendar.MINUTE]}"
                notification.notify(context, null, message, R.drawable.ic_stock_hause)
            }
        }
        return alarmIntent
    }

    fun cancelNetSchedule(context : Context, requestCode : Int)
    {
        val intent = Intent(context, ScheduleReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE)
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        intent?.let {
            alarmManager.cancel(intent)
            println("Расписание отменено...")
            notification.notify(context, null, "Расписание отменено...", R.drawable.ic_stock_hause)
//            alarmManager.cancel {
//                notification.notify(context, null, "Расписание отменено...", R.drawable.ic_stock_hause)
//            }
        }
    }

    private fun getSearchSetByName(db : AppDao, name : String) : RoomSearchSet = runBlocking {
        val set = withContext(Dispatchers.Default) {
            db.getSetByName(name)
        }
        set
    }

    override fun onDocumentError(throwable : Throwable)
    {
        Toast.makeText(context, throwable.message, Toast.LENGTH_LONG).show()
        throwable.printStackTrace()
        return
    }

    override fun onDealListReady(dealList : ArrayList<Deal>)
    {
        if (dealList.size > 0)
        {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putParcelableArrayListExtra(KEY_DEAL_LIST, dealList)
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val message : String = "Request performed: found ${dealList.size} results\n" +
                    "Washington time - ${utils.chicagoTime(context).hour + 1} : ${utils.chicagoTime(context).minute}"

            notification.notify(context, pendingIntent, message, R.drawable.ic_stock_hause)
        }

        return
    }




}