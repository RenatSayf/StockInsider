package com.renatsayf.stockinsider.service

import android.app.Activity
import android.content.Context
import android.icu.util.TimeZone
import com.renatsayf.stockinsider.MainActivity
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
import java.util.logging.Handler
import javax.inject.Inject
import kotlin.collections.ArrayList

class ServiceTask @Inject constructor(private val context: Context, private val timeZone: TimeZone) : TimerTask(), SearchRequest.Companion.IBackWorkerListener
{
    companion object
    {
        const val TAG : String = "com.renatsayf.stockinsider.service.ServiceTask"
    }

    @Inject
    lateinit var searchRequest : SearchRequest

    @Inject
    lateinit var dbProvider : RoomDBProvider

    @Inject
    lateinit var utils : Utils

    private var db : AppDao

    init {
        App().component.inject(this)
        db = dbProvider.getDao(context)
        searchRequest.setBackWorkerListener(this)
    }

    override fun run()
    {
        val (isFilingTime, isAfterFiling) = IsFilingTime.checking(timeZone)
        when(isFilingTime)
        {
            true ->
            {
                db = dbProvider.getDao(context)
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
                println("************* ${requestParams.searchName}: параметры запроса получены **************************")
                when (IsWeekEnd.checking(Date(System.currentTimeMillis()), timeZone)) {
                    false -> {
                        searchRequest.getTradingScreen(TAG, requestParams)
                        //notification.notify(context, null, "Запрос отправлен...", R.drawable.ic_stock_hause_cold)
                        println("Запрос отправлен.....................................................")
                    }
                }

            }
        }
        println("*********************Tick***********************")
    }

    private fun getSearchSetByName(db : AppDao, name : String) : RoomSearchSet = runBlocking {
        val set = withContext(Dispatchers.Default) {
            db.getSetByName(name)
        }
        set
    }

    override fun onDocumentError(throwable: Throwable)
    {
        println("********************* ${throwable.message} *********************************")
    }

    override fun onDealListReady(dealList: ArrayList<Deal>)
    {
        println("********************* ${dealList.size} reslts found *********************************")
    }

}