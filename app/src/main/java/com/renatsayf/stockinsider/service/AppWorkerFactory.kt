package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.modules.NetRepositoryModule
import com.renatsayf.stockinsider.di.modules.RoomDataBaseModule

class AppWorkerFactory(
    private val callback: ((Context, Int, RoomSearchSet) -> Unit)
) : WorkerFactory() {

    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): AppWorker? {
        return when(workerClassName) {
            AppWorker::class.java.name -> {
                val appWorker = AppWorker(appContext, workerParameters).apply {
                    injectDependencies(
                        db = RoomDataBaseModule.provideRoomDataBase(appContext),
                        networkRepository = NetRepositoryModule.provideSearchRequest(NetRepositoryModule.api(appContext)),
                        callback
                    )
                }
                appWorker
            }
            else -> null
        }
    }
}