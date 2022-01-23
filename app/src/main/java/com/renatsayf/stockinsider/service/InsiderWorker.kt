package com.renatsayf.stockinsider.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class InsiderWorker constructor(
    private val context: Context,
    parameters: WorkerParameters): Worker(context, parameters)
{
    companion object {

        val TAG = this::class.java.simpleName.plus(".Tag")
        val SEARCH_SET_KEY = this::class.java.simpleName.plus(".SearchSetKey")

        private val constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.CONNECTED)
        }.build()

        private val requestList = mutableListOf<PeriodicWorkRequest>()

        fun addWorkRequest(requestName: String) {
            val request = PeriodicWorkRequest.Builder(InsiderWorker::class.java, 15, TimeUnit.MINUTES).apply {
                val data = Data.Builder().putString(SEARCH_SET_KEY, requestName).build()
                setInputData(data)
                setConstraints(constraints)
            }.build()

            requestList.add(request)
        }

        fun getWorkRequests(): List<PeriodicWorkRequest> {
            return requestList
        }

        fun enqueueAllRequests(context: Context) {
            WorkManager.getInstance(context).enqueue(requestList)
        }
    }

    @Inject
    lateinit var db: AppDao

    private var utils : Utils = Utils()

    private val composite = CompositeDisposable()

    override fun doWork(): Result
    {
        return try
        {
            val setName = inputData.getString(SEARCH_SET_KEY)
            setName?.let { name ->
                runAlarmTask(name)
                Result.success()
            } ?: run {
                Result.failure()
            }
        }
        catch (e: Exception)
        {
            val message = e.message ?: "********** Unknown error **********"
            val data = Data.Builder().apply {
                putString("error", "*********** $message *************")
            }.build()
            Result.failure(data)
        }
    }

    private fun getSearchSet(setName: String) : RoomSearchSet = runBlocking {
        return@runBlocking withContext(Dispatchers.Default) {
            db.getSetByName(setName)
        }
    }

    private fun runAlarmTask(setName: String)
    {
        val roomSearchSet = getSearchSet(setName)
        val requestParams = roomSearchSet.toSearchSet()

        composite.add(SearchRequest().getTradingScreen(requestParams)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list: ArrayList<Deal>? ->
                list?.let {
                    onDealListReady(context, list)
                }
            }, { e ->
                onDocumentError(context, e)
            }))

    }

    private fun onDocumentError(context: Context, throwable: Throwable)
    {
        composite.apply {
            dispose()
            clear()
        }
        throwable.printStackTrace()
        val time = utils.getFormattedDateTime(0, Calendar.getInstance().time)
        val message = "$time (в.мест) \n" +
                "An error occurred while executing the request:\n" +
                "${throwable.message}"
        ServiceNotification().createNotification(context = context, pendingIntent = null, text = message).show()
    }

    private fun onDealListReady(context: Context, dealList: ArrayList<Deal>)
    {
        composite.apply {
            dispose()
            clear()
        }
        val time = utils.getFormattedDateTime(0, Calendar.getInstance().time)
        val message = "The request has been performed at \n" +
                "$time (в.мест) \n" +
                "${dealList.size} results found"
        val intent = Intent(context, MainActivity::class.java).apply {
            putParcelableArrayListExtra(Deal.KEY_DEAL_LIST, dealList)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK //or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        ServiceNotification().createNotification(context = context, pendingIntent = pendingIntent, text = message).show()
    }
}