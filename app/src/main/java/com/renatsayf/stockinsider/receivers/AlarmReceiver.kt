package com.renatsayf.stockinsider.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.ui.main.MainFragment
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.AppLog
import com.renatsayf.stockinsider.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class AlarmReceiver : BroadcastReceiver()
{
    companion object
    {
        val TAG : String = this::class.java.simpleName
        const val IS_ALARM_SET_KEY = "com.renatsayf.stockinsider.receivers.is_alarm_set"
    }

    @Inject
    lateinit var utils : Utils

    @Inject
    lateinit var notification: ServiceNotification

    @Inject
    lateinit var db : AppDao

    @Inject
    lateinit var searchRequest : SearchRequest

    @Inject
    lateinit var appCalendar: AppCalendar

    @Inject
    lateinit var appLog: AppLog

    private var disposable: Disposable? = null

    init
    {
        MainActivity.appComponent.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?)
    {
        context?.let {
            var message = "******  Alarm has been fired at ${utils.getFormattedDateTime(0, Date(System.currentTimeMillis()))}  **************************"
            appLog.print(TAG, message)
            notification.createNotification(context, null, message, R.drawable.ic_stock_hause_cold, R.color.colorRed).show()

            val isAlarmSetup = context
                .getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(IS_ALARM_SET_KEY, false)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            when(isAlarmSetup)
            {
                true ->
                {
                    runAlarmTask(it)
                    val nextCalendar = appCalendar.getNextCalendar()
                    AlarmPendingIntent.create(context, nextCalendar).let { result ->
                        alarmManager.apply {
                            setExact(AlarmManager.RTC, result.time, result.instance)
                        }
                    }
                }
                else ->
                {
                    AlarmPendingIntent.instance?.let {intent ->
                        alarmManager.cancel(intent)
                    }
                    message = "********  Alarm is canceled  *********************"
                    appLog.print(TAG, message)
                    notification.createNotification(context, null, message, R.drawable.ic_stock_hause_cold, R.color.colorRed).show()
                }
            }
        }
    }

    private fun getSearchSet() : RoomSearchSet = runBlocking {
        return@runBlocking withContext(Dispatchers.Default) {
            db.getSetByName(MainFragment.DEFAULT_SET)
        }
    }

    private fun runAlarmTask(context: Context)
    {
        val roomSearchSet = getSearchSet()
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
        appLog.print(TAG, "******* ${requestParams.searchName}: параметры запроса получены *******************")

        disposable = searchRequest.getTradingScreen(requestParams)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list: ArrayList<Deal>? ->
                           list?.let {
                               onDealListReady(context, list)
                           }
                       }, { e ->
                           onDocumentError(context, e)
                       })

    }

    private fun onDocumentError(context: Context, throwable: Throwable)
    {
        println("********************* ${throwable.message} *********************************")
        disposable?.dispose()
        throwable.printStackTrace()
        val time = utils.getFormattedDateTime(0, Calendar.getInstance().time)
        val message = "$time (в.мест) \n" +
                "Во время выполнения запроса произошла ошибка:\n" +
                "${throwable.message}"
        appLog.print(TAG, message)
        notification.createNotification(context, null, message, R.drawable.ic_stock_hause_cold, R.color.colorRed).show()
    }

    private fun onDealListReady(context: Context, dealList: ArrayList<Deal>)
    {
        disposable?.dispose()
        val time = utils.getFormattedDateTime(0, Calendar.getInstance().time)
        val message = "The request has been performed at \n" +
                "$time (в.мест) \n" +
                "${dealList.size} reslts found"
        appLog.print(TAG, "********************* $message *********************************")
        val intent = Intent(context, MainActivity::class.java).apply {
            putParcelableArrayListExtra(Deal.KEY_DEAL_LIST, dealList)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        notification.createNotification(context, pendingIntent, message, R.drawable.ic_stock_hause_cold, R.color.colorLightGreen).show()
    }

}