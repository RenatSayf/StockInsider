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


class NetIntentService constructor(): IntentService("NetIntentService") {

    companion object {

        private const val serviceID = 2358975

        var isStarted = false
        private set

        var isCompleted = false
        private set

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
        fun start(
            context: Context,
            db: AppDao = RoomDataBaseModule.provideRoomDataBase(context),
            net: INetRepository = NetRepositoryModule.provideSearchRequest(NetRepositoryModule.api(context))
        ) {
            val intent = Intent(context, NetIntentService::class.java).apply {

            }
            context.startService(intent)
            this.db = db
            this.net = net
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        isCompleted = false
        isStarted = true
        return START_STICKY
    }

    override fun onHandleIntent(intent: Intent?) {
        super.onCreate()

        if (BuildConfig.DEBUG) println("******************** Start background work ********************")

        val notification = RequestNotification(this)
        startForeground(serviceID, notification.create())

        Thread.sleep(10000)

        notification.cancel()
        isCompleted = true
        isStarted = false
        if (BuildConfig.DEBUG) println("******************** Background work completed successfully ********************")
    }

    private suspend fun doWork(): Deferred<List<Deal>> {
        return coroutineScope {
            async {

                try {
                    if (searchSets == null) searchSets = getTrackingSetsAsync().await()

                    val dealList = searchSets?.flatMap { set ->
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
                    if (BuildConfig.DEBUG) println("******************** Background work completed successfully ********************")
                    dealList?: emptyList()

                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        println("********************** catch block - Background work failed *****************************")
                        throw Exception(e.message, e.cause)
                    }
                    emptyList()
                }
            }
        }
    }

    private suspend fun getTrackingSetsAsync() : Deferred<List<RoomSearchSet>?> = coroutineScope {
        async {
            db.getTrackedSets(target = Target.Tracking, isTracked = 1)
        }
    }


}