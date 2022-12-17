@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.work.*
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.modules.NetRepositoryModule
import com.renatsayf.stockinsider.di.modules.RoomDataBaseModule
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.network.INetRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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
    private var function: ((Context, Int, RoomSearchSet) -> Unit)? = ServiceNotification.notify

    init {
        db = RoomDataBaseModule.provideRoomDataBase(context)
        net = NetRepositoryModule.provideSearchRequest(NetRepositoryModule.api(context))
    }

    fun injectDependencies(
        db: AppDao,
        networkRepository: INetRepository,
        function: ((Context, Int, RoomSearchSet) -> Unit)? = null
    ) {
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

            searchSets?.forEachIndexed { index, set ->

                val duration = (10..20).random()
                delay(duration * 1000L)
                val params = set.toSearchSet()
                val subscribe = net.getTradingScreen(params)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ list ->
                    if (list.isNotEmpty()) {
                        function?.invoke(context, list.size, set)
                        if (index == searchSets.size - 1) {
                            composite.dispose()
                            composite.clear()
                        }
                        Result.success()
                        if (BuildConfig.DEBUG) println("******************** Background work completed successfully ********************")
                    }
                }, { t ->
                    if (BuildConfig.DEBUG) t.printStackTrace()
                    if (index == searchSets.size - 1) {
                        composite.dispose()
                        composite.clear()
                    }
                    Result.failure()
                    if (BuildConfig.DEBUG) println("********************** Background work failed *****************************")
                })
                subscribe?.let { sub -> composite.add(sub) }
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

}

