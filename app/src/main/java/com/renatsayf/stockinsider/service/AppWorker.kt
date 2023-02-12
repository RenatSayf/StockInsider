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
import com.renatsayf.stockinsider.service.notifications.RequestNotification
import com.renatsayf.stockinsider.service.notifications.ServiceNotification
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.getNextStartTime
import com.renatsayf.stockinsider.utils.timeToFormattedStringWithoutSeconds
import kotlinx.coroutines.*
import java.util.*


class AppWorker (
    private val context: Context,
    parameters: WorkerParameters): CoroutineWorker(context, parameters) {

    companion object {
        val TAG = this::class.java.simpleName.plus(".Tag")
        val SEARCH_SET_KEY = this::class.java.simpleName.plus(".SearchSetKey")

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        var state: State = State.Initial
        private set

        private lateinit var db: AppDao
        private lateinit var net: INetRepository
        private var searchSets: List<RoomSearchSet>? = null
        private var function: ((Context, String, RoomSearchSet) -> Unit)? = ServiceNotification.notify
        private var trackingPeriodInMinutes: Long = FireBaseConfig.trackingPeriod
        private var isTestMode: Boolean = false

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun injectDependenciesToTest(
            searchSets: List<RoomSearchSet>? = null,
            trackingPeriodInMinutes: Long,
            isTestMode: Boolean
        ) {
            this.searchSets = searchSets
            this.trackingPeriodInMinutes = trackingPeriodInMinutes
            this.isTestMode = isTestMode
        }
    }

    init {
        db = RoomDataBaseModule.provideRoomDataBase(context)
        net = NetRepositoryModule.provideSearchRequest(NetRepositoryModule.api(context))
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {

        val notification = RequestNotification(context).create()
        return ForegroundInfo(RequestNotification.NOTIFY_ID, notification!!)
    }

    override suspend fun doWork(): Result
    {
        state = State.Started
        return try
        {
            if (BuildConfig.DEBUG) println("******************** ${this.javaClass.simpleName}: Start background work ********************")

            setForeground(getForegroundInfo())

            if (searchSets == null) searchSets = getTrackingSetsAsync().await()
            val array = searchSets?.map { set ->
                val duration = (10..20).random()
                delay(duration * 1000L)
                val params = set.toSearchSet()
                val deals = net.getDealsListAsync(params).await()

                val nextFillingTime = AppCalendar().getNextStartTime(trackingPeriodInMinutes, isTestMode).timeToFormattedStringWithoutSeconds()
                val message = context.getString(com.renatsayf.stockinsider.R.string.text_by_search) +
                            " ${set.queryName}, ${deals.size} ${context.getString(com.renatsayf.stockinsider.R.string.text_results_were_found)}\n" +
                            "${context.getString(com.renatsayf.stockinsider.R.string.text_next_check_will_be_at)} $nextFillingTime"
                when {
                    BuildConfig.DEBUG -> {
                        function?.invoke(context, message, set)
                    }
                    else -> {
                        if (deals.isNotEmpty()) {
                            function?.invoke(context, message, set)
                        }
                    }
                }
                deals.isNotEmpty()
            }?.toBooleanArray()?: booleanArrayOf()

            val data = Data.Builder().apply {
                putBooleanArray("result", array)
            }.build()
            if (BuildConfig.DEBUG) println("******************** ${this.javaClass.simpleName}: Background work completed successfully ********************")
            state = State.Completed
            Result.success(data)
        }
        catch (e: Exception)
        {
            if (BuildConfig.DEBUG) e.printStackTrace()
            val message = e.message ?: "********** Unknown error **********"
            val errorData = Data.Builder().apply {
                putString("error", "*********** $message *************")
            }.build()
            if (BuildConfig.DEBUG) println("********************** ${this.javaClass.simpleName}: catch block - Background work failed *****************************")
            state = State.Failed
            Result.failure(errorData)
        }
    }

    private suspend fun getTrackingSetsAsync() : Deferred<List<RoomSearchSet>?> = coroutineScope {
        async {
            db.getTrackedSets(target = Target.Tracking, isTracked = 1)
        }
    }

    sealed class State {
        object Initial: State()
        object Started: State()
        object Completed: State()
        object Failed: State()
    }

}

