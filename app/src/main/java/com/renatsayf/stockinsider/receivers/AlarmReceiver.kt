package com.renatsayf.stockinsider.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import com.hypertrack.hyperlog.HyperLog
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.service.ServiceTask
import com.renatsayf.stockinsider.ui.main.MainFragment
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.IsFilingTime
import com.renatsayf.stockinsider.utils.IsWeekEnd
import com.renatsayf.stockinsider.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

    private var disposable: Disposable? = null

    init
    {
        MainActivity.appComponent.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?)
    {
        context?.let {
            val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
            val calendar = Calendar.getInstance(timeZone)
            var message = "******  Alarm fired at ${utils.getFormattedDateTime(0, calendar.time)}  **************************"
            //println(message)
            HyperLog.d(TAG, message)
            notification.createNotification(context, null, message, R.drawable.ic_stock_hause_cold, R.color.colorRed).show()

            val isAlarmSettings = it.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(IS_ALARM_SET_KEY, false)
            val checking = IsFilingTime.checking(timeZone)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            when(checking.isFilingTime && !checking.isAfterFiling && isAlarmSettings)
            {
                true ->
                {
                    runAlarmTask(it)
                    calendar.timeInMillis += IsFilingTime.LOAD_INTERVAL
                    AlarmPendingIntent.create(context, calendar).let {res ->
                        alarmManager.apply {
                            setExact(AlarmManager.RTC, res.time, res.instance)
                        }
                        message = "******  Alarm setup at ${utils.getFormattedDateTime(0, calendar.time)}  **************************"
                        //println(message)
                        HyperLog.d(TAG, message)
                        notification.createNotification(context, null, message, R.drawable.ic_stock_hause_cold, R.color.colorRed).show()
                    }
                }
                false ->
                {
                    AlarmPendingIntent.instance?.let {intent ->
                        alarmManager.cancel(intent)

                        if (!checking.isFilingTime && isAlarmSettings)
                        {
                            if (checking.isAfterFiling)
                            {
                                var amount = 0
                                while (IsWeekEnd.checking(calendar.time, timeZone))
                                {
                                    calendar.apply {
                                        add(Calendar.DATE, amount)
                                        set(Calendar.HOUR_OF_DAY, IsFilingTime.START_HOUR)
                                        set(Calendar.MINUTE, IsFilingTime.START_MINUTE)
                                    }
                                    amount++
                                }

                                AlarmPendingIntent.create(context, calendar).let {res ->
                                    alarmManager.apply {
                                        setExact(AlarmManager.RTC, res.time, res.instance)
                                    }
                                }
                            }
                            else
                            {
                                calendar.apply {
                                    set(Calendar.HOUR_OF_DAY, IsFilingTime.START_HOUR)
                                    set(Calendar.MINUTE, IsFilingTime.START_MINUTE)
                                }



                                AlarmPendingIntent.create(context, calendar).let { res ->
                                    alarmManager.apply {
                                        setExact(AlarmManager.RTC, res.time, res.instance)
                                    }
                                }
                            }
                        }

                        message = "********  Alarm is canceled  *********************"
                        HyperLog.d(TAG, message)
                        notification.createNotification(context, null, message, R.drawable.ic_stock_hause_cold, R.color.colorRed).show()

                    }
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
        HyperLog.d(ServiceTask.TAG, "******* ${requestParams.searchName}: параметры запроса получены *******************")

        disposable = searchRequest.getTradingScreen(requestParams)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list: java.util.ArrayList<Deal>? ->
                           list?.let {
                               onDealListReady(context, list)
                           }
                       }, { e ->
                           onDocumentError(context, e)
                       })

        //notification.notify(context, null, "Запрос отправлен...", R.drawable.ic_stock_hause_cold)
        println("Запрос отправлен.....................................................")
    }

    private fun onDocumentError(context: Context, throwable: Throwable)
    {
        println("********************* ${throwable.message} *********************************")
        disposable?.dispose()
        throwable.printStackTrace()
        val time = utils.getFormattedDateTime(0, java.util.Calendar.getInstance().time)
        val message = "$time (в.мест) \n" +
                "Во время выполнения запроса произошла ошибка:\n" +
                "${throwable.message}"
        HyperLog.d(ServiceTask.TAG, message)
        notification.createNotification(context, null, message, R.drawable.ic_stock_hause_cold, R.color.colorRed).show()
    }

    private fun onDealListReady(context: Context, dealList: ArrayList<Deal>)
    {
        disposable?.dispose()
        val time = utils.getFormattedDateTime(0, java.util.Calendar.getInstance().time)
        val message = "The request has been performed at \n" +
                "$time (в.мест) \n" +
                "${dealList.size} reslts found"
        HyperLog.d(ServiceTask.TAG, "********************* $message *********************************")
        val intent = Intent(context, MainActivity::class.java).apply {
            putParcelableArrayListExtra(ServiceTask.KEY_DEAL_LIST, dealList)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        notification.createNotification(context, pendingIntent, message, R.drawable.ic_stock_hause_cold, R.color.colorLightGreen).show()
    }

}