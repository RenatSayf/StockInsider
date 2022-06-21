@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.work.*
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.network.INetworkRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject


class AppWorker @Inject constructor(
    private val context: Context,
    parameters: WorkerParameters): CoroutineWorker(context, parameters) {

    companion object {
        val TAG = this::class.java.simpleName.plus(".Tag")
        val SEARCH_SET_KEY = this::class.java.simpleName.plus(".SearchSetKey")
    }

    private val composite = CompositeDisposable()

    @Inject
    lateinit var db: AppDao

    @Inject
    lateinit var networkRepository: INetworkRepository
    private var function: ((Context, ArrayList<Deal>) -> Unit)? = null

    fun injectDependencies(db: AppDao, networkRepository: INetworkRepository, function: ((Context, ArrayList<Deal>) -> Unit)? = null) {
        this.db = db
        this.networkRepository = networkRepository
        this.function = function
    }

    override suspend fun doWork(): Result
    {
        return try
        {
            val setName = inputData.getString(SEARCH_SET_KEY)
            setName?.let { name ->
                val result = performPayload(name)
                when(result) {
                    is WorkerResult.Success -> {
                        function?.invoke(context, result.deals)
                        composite.dispose()
                        composite.clear()
                        Result.success()
                    }
                    is WorkerResult.Error -> {
                        composite.dispose()
                        composite.clear()
                        Result.failure()
                    }
                }
            } ?: run {
                composite.dispose()
                composite.clear()
                Result.failure()
            }
        }
        catch (e: Exception)
        {
            composite.dispose()
            composite.clear()
            val message = e.message ?: "********** Unknown error **********"
            val data = Data.Builder().apply {
                putString("error", "*********** $message *************")
            }.build()
            Result.failure(data)
        }
    }

    private suspend fun getSearchSetAsync(setName: String) : Deferred<RoomSearchSet?> = coroutineScope {
        async {
            db.getSetByName(setName)
        }
    }

    private suspend fun performPayload(setName: String) : WorkerResult
    {
        var result: WorkerResult = WorkerResult.Error(Throwable("Unknown error"))

        val roomSearchSet = getSearchSetAsync(setName).await()
        val requestParams = roomSearchSet?.toSearchSet()

        if (roomSearchSet != null && requestParams != null) {

            val subscribe = networkRepository.getTradingScreen(requestParams).subscribe({ list: ArrayList<Deal>? ->
                if (!list.isNullOrEmpty()) {
                    result = WorkerResult.Success(list)
                }
            }, { e ->
                result = WorkerResult.Error(e)
            })
            subscribe?.let { composite.add(it) }
        }

        return result
    }

//    private fun onDocumentError(context: Context, throwable: Throwable)
//    {
//        composite.apply {
//            dispose()
//            clear()
//        }
//        throwable.printStackTrace()
//        val time = Utils().getFormattedDateTime(0, Calendar.getInstance().time)
//        val message = "$time (в.мест) \n" +
//                "An error occurred while executing the request:\n" +
//                "${throwable.message}"
//        ServiceNotification().createNotification(context = context, pendingIntent = null, text = message).show()
//    }

//    private fun onDealListReady(context: Context, dealList: ArrayList<Deal>)
//    {
//        val time = Utils().getFormattedDateTime(0, Calendar.getInstance().time)
//        val message = "The request has been performed at \n" +
//                "$time (в.мест) \n" +
//                "${dealList.size} results found"
//        val intent = Intent(context, MainActivity::class.java).apply {
//            putParcelableArrayListExtra(Deal.KEY_DEAL_LIST, dealList)
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK //or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        ServiceNotification().createNotification(context = context, pendingIntent = pendingIntent, text = message).show()
//    }

    sealed class WorkerResult {
        data class Success(val deals: ArrayList<Deal>): WorkerResult()
        data class Error(val throwable: Throwable): WorkerResult()
    }
}

