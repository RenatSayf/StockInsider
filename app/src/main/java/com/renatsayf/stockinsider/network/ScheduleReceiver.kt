package com.renatsayf.stockinsider.network

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.NotificationCompat
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
import java.util.*
import javax.inject.Inject


class ScheduleReceiver @Inject constructor() : BroadcastReceiver(), SearchRequest.Companion.IBackWorkerListener
{
    companion object
    {
        val TAG : String = this.hashCode().toString()
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
        val action = intent?.action
        context?.let {
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

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 53)
        }

        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 30000L,
                60000L,
                alarmIntent
                                 )
        val creatorUid = alarmIntent.creatorUid
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
        return
    }

    override fun onDealListReady(dealList : ArrayList<Deal>)
    {
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
        val builder = NotificationCompat.Builder(context, chanelId)
            .setSmallIcon(R.drawable.ic_menu_send)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("Запрос выполнен")
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        notificationManager.notify(1111, builder.build())
        //Toast.makeText(context, "Запрос выполнен " + dealList.toString(), Toast.LENGTH_LONG).show()
        return
    }




}