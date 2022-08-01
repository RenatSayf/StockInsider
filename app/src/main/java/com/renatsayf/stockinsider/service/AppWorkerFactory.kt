package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.network.INetRepository

class AppWorkerFactory(private val db: AppDao,
                        private val net: INetRepository) : WorkerFactory() {

    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): Worker? {
        return when(workerClassName) {
            AppWorker::class.java.name -> {
                //AppWorker(appContext, workerParameters, db, net)
                null
            }
            else -> null
        }
    }
}