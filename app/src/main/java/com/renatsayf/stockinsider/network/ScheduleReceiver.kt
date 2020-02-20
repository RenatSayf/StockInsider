package com.renatsayf.stockinsider.network

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.Toast
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


class ScheduleReceiver @Inject constructor() : BroadcastReceiver(), SearchRequest.Companion.IDocumentListener
{
    @Inject
    lateinit var dbProvider : RoomDBProvider

    @Inject
    lateinit var utils : Utils

    @Inject
    lateinit var searchRequest : SearchRequest

    private lateinit var db : AppDao

    private lateinit var context : Context

    init
    {
        App().component.inject(this)
    }

    override fun onReceive(context : Context?, intent : Intent?)
    {
        val action = intent?.action
        context?.let {
            searchRequest.setOnDocumentReadyListener(this)
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
            searchRequest.getTradingScreen(requestParams)
        }
        return
    }

    fun setNetSchedule(context : Context) : PendingIntent
    {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, 0)
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
        return alarmIntent
    }

    fun cancelNetSchedule(context : Context)
    {
        val intent = Intent(context, ScheduleReceiver::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
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
        Toast.makeText(context, "Запрос выполнен " + dealList.toString(), Toast.LENGTH_LONG).show()
        return
    }
}