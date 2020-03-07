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
import com.renatsayf.stockinsider.ui.home.HomeFragment
import com.renatsayf.stockinsider.utils.IsWeekEnd
import com.renatsayf.stockinsider.utils.Utils
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
        private const val START_HOUR : Int = 6
        private const val END_HOUR : Int = 21
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
            if (utils.chicagoTime(context).hour > END_HOUR || utils.chicagoTime(context).hour < START_HOUR)
            {
                cancelNetSchedule(it, REQUEST_CODE)
                setNetSchedule(it, REQUEST_CODE)
                return
            }
            searchRequest.setBackWorkerListener(this)
            this.context = context
            db = dbProvider.getDao(it)
            val roomSearchSet = getSearchSetByName(db, HomeFragment.DEFAULT_SET)
            val requestParams = SearchSet(HomeFragment.DEFAULT_SET).apply {
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

            val isWeekEnd = IsWeekEnd.checking(
                    Date(System.currentTimeMillis()),
                    TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
                                             )
            when(isWeekEnd)
            {
                false -> searchRequest.getTradingScreen(TAG, requestParams)
            }
        }
        return
    }

    fun setNetSchedule(context : Context, requestCode : Int) : PendingIntent
    {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, requestCode, intent, 0)
        }

        val startHour = START_HOUR
        val interval : Long = AlarmManager.INTERVAL_HOUR
        val chicagoTime = utils.chicagoTime(context)
        println("chicagoTime.hour = ${chicagoTime.hour}")
        if (chicagoTime.hour in (startHour + 1)..END_HOUR)
        {
            val calendar = Calendar.getInstance().apply {
                clear()
                timeInMillis = System.currentTimeMillis()
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, interval, alarmIntent)
        }
        else if (chicagoTime.hour <= startHour)
        {
            val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
            val calendar = Calendar.getInstance(timeZone).apply {
                clear()
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, chicagoTime.minute)
            }

            val patternDateTime = "dd.MM.yyyy  HH:mm"
            val dateFormat = SimpleDateFormat(patternDateTime, Locale.getDefault())
            val startTime = dateFormat.format(calendar.time)
            val currentTime = Date(System.currentTimeMillis())
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, interval, alarmIntent)
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

            notification.notify(context, pendingIntent, message, R.drawable.ic_public_green)
        }

        return
    }




}