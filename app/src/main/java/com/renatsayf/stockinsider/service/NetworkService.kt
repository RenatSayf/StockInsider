package com.renatsayf.stockinsider.service

import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.VisibleForTesting
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.modules.NetRepositoryModule
import com.renatsayf.stockinsider.di.modules.RoomDataBaseModule
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.network.INetRepository
import com.renatsayf.stockinsider.service.notifications.RequestNotification
import com.renatsayf.stockinsider.service.notifications.ServiceNotification
import kotlinx.coroutines.*


class NetworkService : Service() {

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
    }

    private var job: Job? = null

    fun start(context: Context): ComponentName? {
        val intent = Intent(context, NetworkService::class.java)
        return context.startService(intent)
    }

    fun stop(context: Context): Boolean {
        val intent = Intent(context, NetworkService::class.java)
        return context.stopService(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        db = RoomDataBaseModule.provideRoomDataBase(this)
        net = NetRepositoryModule.provideSearchRequest(NetRepositoryModule.api(this))

        if (BuildConfig.DEBUG) println("******************** Start background work ********************")

        val notification = RequestNotification(this@NetworkService)

        try {
            job = CoroutineScope(Dispatchers.IO).launch {
                if (searchSets == null) searchSets = getTrackingSetsAsync().await()

                withContext(Dispatchers.Main) {
                    searchSets?.flatMap { set ->
                        val params = set.toSearchSet()
                        val deals = net.getDealsListAsync(params).await()
                        when {
                            BuildConfig.DEBUG -> function?.invoke(
                                this@NetworkService,
                                deals.size,
                                set
                            )
                            else -> {
                                if (deals.isNotEmpty()) {
                                    function?.invoke(this@NetworkService, deals.size, set)
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
                stopSelf()
                stopForeground(true)
            }
            startForeground(serviceID, notification.create())

        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) println("********************** catch block - Background work failed *****************************")
                throw Exception(e.message, e.cause)
            }
            isStarted = false
            stopSelf()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onStart(intent: Intent?, startId: Int) {
        isStarted = true
        isCompleted = false
    }

    private suspend fun getTrackingSetsAsync() : Deferred<List<RoomSearchSet>?> = coroutineScope {
        async {
            db.getTrackedSets(target = Target.Tracking, isTracked = 1)
        }
    }

    override fun onDestroy() {

        isStarted = false
        job?.cancel()
        super.onDestroy()
    }

    override fun onLowMemory() {

        isStarted = false
        job?.cancel()
        stopSelf()
        super.onLowMemory()
    }

}