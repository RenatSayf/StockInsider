package com.renatsayf.stockinsider.service

import android.app.IntentService
import android.content.Intent
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.modules.NetRepositoryModule
import com.renatsayf.stockinsider.di.modules.RoomDataBaseModule
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.network.INetRepository
import com.renatsayf.stockinsider.service.notifications.RequestNotification
import com.renatsayf.stockinsider.service.notifications.ServiceNotification
import kotlinx.coroutines.*


class NetIntentService : IntentService("NetIntentService") {

    companion object {

        private const val serviceID = 357895124
        var isStarted = false
        var isCompleted = true

        private lateinit var db: AppDao
        private lateinit var net: INetRepository
        private var searchSets: List<RoomSearchSet>? = null
        private var function: ((Context, Int, RoomSearchSet) -> Unit)? = ServiceNotification.notify

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun injectDependenciesToTest(
            searchSets: List<RoomSearchSet>? = null
        ) {
            this.searchSets = searchSets
        }

        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, NetIntentService::class.java).apply {

            }
            context.startService(intent)
        }
    }

    override fun onStart(intent: Intent?, startId: Int) {

    }

    override fun onHandleIntent(intent: Intent?) {

        db = RoomDataBaseModule.provideRoomDataBase(this)
        net = NetRepositoryModule.provideSearchRequest(NetRepositoryModule.api(this))

        if (BuildConfig.DEBUG) println("******************** Start background work ********************")

        val notification = RequestNotification(this)

        try {
            CoroutineScope(Dispatchers.IO).launch {
                if (searchSets == null) searchSets = getTrackingSetsAsync().await()

                withContext(Dispatchers.Main) {
                    searchSets?.flatMap { set ->
                        val params = set.toSearchSet()
                        val deals = net.getDealsListAsync(params).await()
                        when {
                            BuildConfig.DEBUG -> function?.invoke(
                                this@NetIntentService,
                                deals.size,
                                set
                            )
                            else -> {
                                if (deals.isNotEmpty()) {
                                    function?.invoke(this@NetIntentService, deals.size, set)
                                }
                            }
                        }
                        val duration = (10..20).random()
                        delay(duration * 1000L)
                        deals
                    }
                }
                notification.cancel()
                if (BuildConfig.DEBUG) println("******************** Background work completed successfully ********************")
                isCompleted = true
                isStarted = false
            }
            startForeground(serviceID, notification.create())

        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) println("********************** catch block - Background work failed *****************************")
                throw Exception(e.message, e.cause)
            }
            NetworkService.isStarted = false
        }
    }

    private suspend fun doWork(): Deferred<List<Deal>> {
        return coroutineScope {
            async {

                emptyList()
            }
        }
    }

    private suspend fun getTrackingSetsAsync() : Deferred<List<RoomSearchSet>?> = coroutineScope {
        async {
            db.getTrackedSets(target = Target.Tracking, isTracked = 1)
        }
    }


}