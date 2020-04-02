package com.renatsayf.stockinsider.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.TimeZone
import com.hypertrack.hyperlog.HyperLog
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.ui.main.MainFragment
import com.renatsayf.stockinsider.utils.IsFilingTime
import com.renatsayf.stockinsider.utils.IsWeekEnd
import com.renatsayf.stockinsider.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ServiceTask @Inject constructor(private val context: Context, private val timeZone: TimeZone) : TimerTask()
{
    companion object
    {
        val TAG : String = this::class.java.simpleName
        const val KEY_DEAL_LIST : String = "key_deal_list"
    }

    @Inject
    lateinit var searchRequest : SearchRequest

    @Inject
    lateinit var notification : ServiceNotification

    @Inject
    lateinit var utils : Utils

    @Inject
    lateinit var db : AppDao

    init {
        MainActivity.appComponent.inject(this)
    }

    override fun run()
    {
        HyperLog.d(TAG, "********** Сработал метод run() *************")
        val (isFilingTime, isAfterFiling) = IsFilingTime.checking(timeZone)
        val isWeekEnd = IsWeekEnd.checking(Date(System.currentTimeMillis()), timeZone)
        when(isFilingTime/* && !isWeekEnd*/)
        {
            true ->
            {
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
                HyperLog.d(TAG, "******* ${requestParams.searchName}: параметры запроса получены *******************")

               searchRequest.getTradingScreen(requestParams)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ list: java.util.ArrayList<Deal>? ->
                        list?.let {
                            HyperLog.d(TAG, "==ArrayList<Deal>====${list.size}=====================")
                            onDealListReady(list)
                        }
                    }, { e ->
                        onDocumentError(e)
                    })

                //notification.notify(context, null, "Запрос отправлен...", R.drawable.ic_stock_hause_cold)
                println("Запрос отправлен.....................................................")
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

    private fun onDocumentError(throwable: Throwable)
    {
        println("********************* ${throwable.message} *********************************")
        throwable.printStackTrace()
        val time = utils.getFormattedDateTime(0, Calendar.getInstance().time)
        val message = "$time (в.мест) \n" +
                "Во время выполнения запроса произошла ошибка:\n" +
                "${throwable.message}"
        HyperLog.d(TAG, message)
        notification.createNotification(context, null, message, R.drawable.ic_stock_hause_cold, R.color.colorRed).show()
    }

    private fun onDealListReady(dealList: ArrayList<Deal>)
    {
        val time = utils.getFormattedDateTime(0, Calendar.getInstance().time)
        val message = "The request has been performed at \n" +
                "$time (в.мест) \n" +
                "${dealList.size} reslts found"
        HyperLog.d(TAG, "********************* $message *********************************")
        val intent = Intent(context, MainActivity::class.java).apply {
            putParcelableArrayListExtra(KEY_DEAL_LIST, dealList)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        notification.createNotification(context, pendingIntent, message, R.drawable.ic_stock_hause_cold, R.color.colorLightGreen).show()
    }

}