package com.renatsayf.stockinsider.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.AppDataBase
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit


class InsiderWorker constructor(private val context: Context,
    parameters: WorkerParameters): Worker(context, parameters)
{
    companion object {

        val TAG = this::class.java.simpleName.plus(".Tag")
        val periodicWorkRequest = PeriodicWorkRequest.Builder(InsiderWorker::class.java, 15, TimeUnit.MINUTES).apply {
            setConstraints(constraints)
        }.build()

        private val constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.CONNECTED)
        }.build()

//        fun createPeriodicRequest(): PeriodicWorkRequest {
//
//        }
    }

    private var utils : Utils = Utils()

    private var disposable: Disposable? = null

    override fun doWork(): Result
    {
        return try
        {
            runAlarmTask()
            Result.success()
        }
        catch (e: Exception)
        {
            Result.failure()
        }
    }

    private fun getSearchSet(setName: String) : RoomSearchSet = runBlocking {
        return@runBlocking withContext(Dispatchers.Default) {
            AppDataBase.getInstance(context).searchSetDao().getSetByName(setName)
        }
    }

    private fun runAlarmTask()
    {
        val roomSearchSet = getSearchSet(context.getString(R.string.text_default_set_name))
        val requestParams = SearchSet(roomSearchSet.queryName).apply {
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

        disposable = SearchRequest().getTradingScreen(requestParams)
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
        disposable?.dispose()
        throwable.printStackTrace()
        val time = utils.getFormattedDateTime(0, Calendar.getInstance().time)
        val message = "$time (в.мест) \n" +
                "An error occurred while executing the request:\n" +
                "${throwable.message}"
        ServiceNotification().createNotification(context = context, pendingIntent = null, text = message).show()
    }

    private fun onDealListReady(context: Context, dealList: ArrayList<Deal>)
    {
        disposable?.dispose()
        val time = utils.getFormattedDateTime(0, Calendar.getInstance().time)
        val message = "The request has been performed at \n" +
                "$time (в.мест) \n" +
                "${dealList.size} reslts found"
        val intent = Intent(context, MainActivity::class.java).apply {
            putParcelableArrayListExtra(Deal.KEY_DEAL_LIST, dealList)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK //or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        ServiceNotification().createNotification(context = context, pendingIntent = pendingIntent, text = message).show()
    }
}