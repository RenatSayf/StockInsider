@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.work.*
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.modules.NetRepositoryModule
import com.renatsayf.stockinsider.di.modules.RoomDataBaseModule
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.network.INetRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import java.util.*


class AppWorker (
    private val context: Context,
    parameters: WorkerParameters): CoroutineWorker(context, parameters) {

    companion object {
        val TAG = this::class.java.simpleName.plus(".Tag")
        val SEARCH_SET_KEY = this::class.java.simpleName.plus(".SearchSetKey")
    }

    private val composite = CompositeDisposable()

    private var db: AppDao
    private var net: INetRepository

    private var function: ((Context, ArrayList<Deal>) -> Unit)? = ServiceNotification.notify

    init {
        db = RoomDataBaseModule.provideRoomDataBase(context)
        net = NetRepositoryModule.provideSearchRequest(NetRepositoryModule.api(context))
    }

    fun injectDependencies(db: AppDao, networkRepository: INetRepository, function: ((Context, ArrayList<Deal>) -> Unit)? = null) {
        this.db = db
        this.net = networkRepository
        this.function = function
    }

    override suspend fun doWork(): Result
    {
        return try
        {
            if (BuildConfig.DEBUG) println("******************** Start background work ********************")
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
                        if (BuildConfig.DEBUG) println("********************** Background work failed *****************************")
                    }
                    is WorkerResult.Success -> {
                        function?.invoke(context, res.deals)
                        composite.dispose()
                        composite.clear()
                        Result.success()
                        if (BuildConfig.DEBUG) println("******************** Background work completed successfully ********************")
                    }
                }
            }
            Result.success()
        }
        catch (e: Exception)
        {
            if (BuildConfig.DEBUG) e.printStackTrace()
            composite.dispose()
            composite.clear()
            val message = e.message ?: "********** Unknown error **********"
            val data = Data.Builder().apply {
                putString("error", "*********** $message *************")
            }.build()
            if (BuildConfig.DEBUG) println("********************** catch block - Background work failed *****************************")
            Result.failure(data)
        }
    }

    private suspend fun getTrackingSetsAsync() : Deferred<List<RoomSearchSet>?> = coroutineScope {
        async {
            db.getTrackedSets(target = Target.Tracking, isTracked = 1)
        }
    }

    private fun performPayload(set: SearchSet) : WorkerResult
    {
        var result: WorkerResult = WorkerResult.Error(Throwable("Unknown error"))

        val subscribe = net.getTradingScreen(set).subscribe({ list: ArrayList<Deal>? ->
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

