package com.renatsayf.stockinsider.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.renatsayf.stockinsider.network.Scheduler
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.ui.main.MainFragment
import com.renatsayf.stockinsider.utils.IsFilingTime
import com.renatsayf.stockinsider.utils.IsWeekEnd
import com.renatsayf.stockinsider.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class GetFilingDataTask @Inject constructor(private val context: Context, private val timer: Timer): TimerTask(), SearchRequest.Companion.IBackWorkerListener
{
    @Inject
    lateinit var scheduler : Scheduler

    @Inject
    lateinit var searchRequest : SearchRequest

    @Inject
    lateinit var dbProvider : RoomDBProvider

    @Inject
    lateinit var utils : Utils

    @Inject
    lateinit var notification : ServiceNotification

    private lateinit var db : AppDao

    init
    {
        App().component.inject(this)
    }
    override fun run()
    {
        receiveData()
    }

    private fun receiveData()
    {
        context.let {
            val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
            val (isFilingTime, isAfterFiling) = IsFilingTime.checking(timeZone)
            if (!isFilingTime) {
                scheduler.cancelNetSchedule(context, timer)
                when(isAfterFiling) {
                    true -> scheduler.setNetSchedule(it, true)
                    else -> scheduler.setNetSchedule(it, false)
                }
                return
            }

            searchRequest.setBackWorkerListener(this)
            db = dbProvider.getDao(it)
            val roomSearchSet = getSearchSetByName(db, MainFragment.DEFAULT_SET)
            val requestParams = SearchSet(MainFragment.DEFAULT_SET).apply {
                ticker = roomSearchSet.ticker
                filingPeriod = utils.getFilingOrTradeValue(context, roomSearchSet.filingPeriod)
                tradePeriod = utils.getFilingOrTradeValue(context, roomSearchSet.tradePeriod)
                isPurchase = utils.convertBoolToString(roomSearchSet.isPurchase)
                isSale = utils.convertBoolToString(roomSearchSet.isSale)
                tradedMin = roomSearchSet.tradedMin
                tradedMax = roomSearchSet.tradedMax
                isOfficer = utils.isOfficerToString(roomSearchSet.isOfficer)
                isDirector = utils.convertBoolToString(roomSearchSet.isDirector)
                isTenPercent = utils.convertBoolToString(roomSearchSet.isTenPercent)
                groupBy = utils.getGroupingValue(context, roomSearchSet.groupBy)
                sortBy = utils.getSortingValue(context, roomSearchSet.sortBy)
            }

            when(IsWeekEnd.checking(Date(System.currentTimeMillis()), timeZone))
            {
                false -> {
                    searchRequest.getTradingScreen(Scheduler.TAG, requestParams)
                    //notification.notify(context, null, "Запрос отправлен...", R.drawable.ic_stock_hause_cold)
                    println("Запрос отправлен...")
                }
            }
//            notification.notify(context, null, "Запрос отправлен...", R.drawable.ic_stock_hause_cold)
//            println("Запрос отправлен...")
        }
        return
    }

    private fun getSearchSetByName(db : AppDao, name : String) : RoomSearchSet = runBlocking {
        val set = withContext(Dispatchers.Default) {
            db.getSetByName(name)
        }
        set
    }

    override fun onDocumentError(throwable: Throwable)
    {
        Toast.makeText(context, throwable.message, Toast.LENGTH_LONG).show()
        throwable.printStackTrace()
        return
    }

    override fun onDealListReady(dealList: ArrayList<Deal>)
    {
        if (dealList.size > 0)
        {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putParcelableArrayListExtra(Scheduler.KEY_DEAL_LIST, dealList)
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val message : String = "Request performed: found ${dealList.size} results\n" +
                    "Washington time - ${utils.chicagoTime(context).hour + 1} : ${utils.chicagoTime(context).minute}"

            notification.notify(context, pendingIntent, message, R.drawable.ic_stock_hause_cold)
        }

        return
    }

}