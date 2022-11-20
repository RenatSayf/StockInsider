@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.work.*
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.network.INetRepository
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
    lateinit var networkRepository: INetRepository

    private var function: ((Context, ArrayList<Deal>) -> Unit)? = ServiceNotification.notify

    fun injectDependencies(db: AppDao, networkRepository: INetRepository, function: ((Context, ArrayList<Deal>) -> Unit)? = null) {
        this.db = db
        this.networkRepository = networkRepository
        this.function = function
    }

    override suspend fun doWork(): Result
    {
        return try
        {
            val searchSets = getTrackingSetsAsync().await()

            val resultList = searchSets?.map {
                val params = it.toSearchSet()
                performPayload(params)
            }
            resultList?.forEach { res ->
                when (res) {
                    is WorkerResult.Error -> {
                        composite.dispose()
                        composite.clear()
                        Result.failure()
                    }
                    is WorkerResult.Success -> {
                        function?.invoke(context, res.deals)
                        composite.dispose()
                        composite.clear()
                        Result.success()
                    }
                }
            }
            Result.success()
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

    private suspend fun getTrackingSetsAsync() : Deferred<List<RoomSearchSet>?> = coroutineScope {
        async {
            db.getSearchSetsByTarget(Target.Tracking)
        }
    }

    private fun performPayload(set: SearchSet) : WorkerResult
    {
        var result: WorkerResult = WorkerResult.Error(Throwable("Unknown error"))

        val subscribe = networkRepository.getTradingScreen(set).subscribe({ list: ArrayList<Deal>? ->
            if (!list.isNullOrEmpty()) {
                result = WorkerResult.Success(list)
            }
        }, { e ->
            result = WorkerResult.Error(e)
        })
        subscribe?.let { composite.add(it) }

        return result
    }

    sealed class WorkerResult {
        data class Success(val deals: ArrayList<Deal>): WorkerResult()
        data class Error(val throwable: Throwable): WorkerResult()
    }


}

