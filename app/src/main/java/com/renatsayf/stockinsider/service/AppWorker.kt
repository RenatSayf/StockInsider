@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.*
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.modules.NetRepositoryModule
import com.renatsayf.stockinsider.di.modules.RoomDataBaseModule
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.network.INetRepository
import com.renatsayf.stockinsider.service.notifications.ServiceNotification
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.cancelBackgroundWork
import com.renatsayf.stockinsider.utils.startOneTimeBackgroundWork
import kotlinx.coroutines.*
import java.util.*


class AppWorker (
    private val context: Context,
    parameters: WorkerParameters): CoroutineWorker(context, parameters) {

    companion object {
        val TAG = this::class.java.simpleName.plus(".Tag")
        val SEARCH_SET_KEY = this::class.java.simpleName.plus(".SearchSetKey")

        var isRunInTest = false

        private lateinit var db: AppDao
        private lateinit var net: INetRepository
        private var searchSets: List<RoomSearchSet>? = null
        private var function: ((Context, Int, RoomSearchSet) -> Unit)? = ServiceNotification.notify

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun injectDependenciesToTest(
            db: AppDao,
            networkRepository: INetRepository,
            searchSets: List<RoomSearchSet>? = null,
            function: ((Context, Int, RoomSearchSet) -> Unit)? = null
        ) {
            this.db = db
            this.net = networkRepository
            this.searchSets = searchSets
            this.function = function
        }
    }

    init {
        db = RoomDataBaseModule.provideRoomDataBase(context)
        net = NetRepositoryModule.provideSearchRequest(NetRepositoryModule.api(context))
    }

    override suspend fun doWork(): Result
    {
        return try
        {
            if (BuildConfig.DEBUG) println("******************** Start background work ********************")

            if (searchSets == null) searchSets = getTrackingSetsAsync().await()
            val array = searchSets?.map { set ->
                val duration = (10..20).random()
                delay(duration * 1000L)
                val params = set.toSearchSet()
                val deals = net.getDealsListAsync(params).await()
                when {
                    BuildConfig.DEBUG -> function?.invoke(context, deals.size, set)
                    else -> {
                        if (deals.isNotEmpty()) {
                            function?.invoke(context, deals.size, set)
                        }
                    }
                }
                deals.isNotEmpty()
            }?.toBooleanArray()?: booleanArrayOf()

            val data = Data.Builder().apply {
                putBooleanArray("result", array)
            }.build()
            if (BuildConfig.DEBUG) println("******************** Background work completed successfully ********************")
            Result.success(data)
        }
        catch (e: Exception)
        {
            if (BuildConfig.DEBUG) e.printStackTrace()
            val message = e.message ?: "********** Unknown error **********"
            val errorData = Data.Builder().apply {
                putString("error", "*********** $message *************")
            }.build()
            if (BuildConfig.DEBUG) println("********************** catch block - Background work failed *****************************")
            Result.failure(errorData)
        }
        finally {

            if (!isRunInTest) {
                if (!searchSets.isNullOrEmpty()) {
                    val nextTimeLong = if (BuildConfig.DEBUG) AppCalendar.getNextTestTimeByUTCTimeZone(
                        workerPeriod = FireBaseConfig.workerPeriod
                    ) else AppCalendar.getNextFillingTimeByUTCTimeZone(
                        workerPeriod = FireBaseConfig.workerPeriod,
                    )
                    context.startOneTimeBackgroundWork(nextTimeLong)
                }
                else context.cancelBackgroundWork()
            }
        }
    }

    private suspend fun getTrackingSetsAsync() : Deferred<List<RoomSearchSet>?> = coroutineScope {
        async {
            db.getTrackedSets(target = Target.Tracking, isTracked = 1)
        }
    }

}

