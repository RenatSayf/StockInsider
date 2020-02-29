package com.renatsayf.stockinsider.network

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomDBProvider
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.ui.home.HomeFragment
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
    }

    @Inject
    lateinit var dbProvider : RoomDBProvider

    @Inject
    lateinit var utils : Utils

    @Inject
    lateinit var searchRequest : SearchRequest

    private lateinit var db : AppDao
    private lateinit var context : Context
    private val chanelId: String = "channel_com.renatsayf.stockinsider.network"

    init
    {
        App().component.inject(this)
    }

    override fun onReceive(context : Context?, intent : Intent?)
    {
        context?.let {
            if (utils.chicagoTimeHour(context) >= 18)
            {
                cancelNetSchedule(it, REQUEST_CODE)
                setNetSchedule(it, REQUEST_CODE)
            }
            searchRequest.setBackWorkerListener(this)
            this.context = context
            db = dbProvider.getDao(it)
            val roomSearchSet = getSearchSetByName(db, HomeFragment.DEFAULT_SET)
            val requestParams = SearchSet(HomeFragment.DEFAULT_SET).apply {
                ticker = roomSearchSet.ticker
                filingPeriod = 1.toString()
                tradePeriod = 1.toString()
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

            searchRequest.getTradingScreen(TAG, requestParams)
        }
        return
    }

    fun setNetSchedule(context : Context, requestCode : Int) : PendingIntent
    {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, requestCode, intent, 0)
        }

        val startHour = 10
        val interval : Long = AlarmManager.INTERVAL_HOUR
        val chicagoTimeHour = utils.chicagoTimeHour(context)
        if (chicagoTimeHour > startHour)
        {
            val calendar = Calendar.getInstance().apply {
                clear()
                timeInMillis = System.currentTimeMillis()
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, interval, alarmIntent)
        }
        else if (chicagoTimeHour <= startHour)
        {
            val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
            val calendar = Calendar.getInstance(timeZone).apply {
                clear()
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, 1)
            }

            val patternDateTime = "dd.MM.yyyy  HH:mm"
            val dateFormat = SimpleDateFormat(patternDateTime, Locale.getDefault())
            val startTime = dateFormat.format(calendar.time)
            val currentTime = Date(System.currentTimeMillis())
            val currentTimeMillis = System.currentTimeMillis()
            val timeInMillis = calendar.timeInMillis
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, interval, alarmIntent)
        }
//        alarmManager.setRepeating(
//                AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() + 30000L,
//                60000L,
//                alarmIntent
//                                 )
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
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val notification = NotificationCompat.Builder(context, chanelId)
                .setSmallIcon(R.drawable.ic_public_green)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Request performed: found ${dealList.size} results\n" +
                                        "Washington time - ${utils.chicagoTimeHour(context)}")
                .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH).setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                val name = context.getString(R.string.app_name).plus(chanelId)
                val descriptionText = "XXXXXXXXXXXX"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(chanelId, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.cancelAll()
            notificationManager.notify(1111, notification)
        }

        return
    }




}