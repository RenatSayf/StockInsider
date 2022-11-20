package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkerParameters
import com.renatsayf.stockinsider.BuildConfig
import java.util.concurrent.TimeUnit


object WorkTask: IWorkTask {

    private val timePeriod = if(BuildConfig.DEBUG) 15L else 480L

    private val taskList = mutableSetOf<PeriodicWorkRequest>()

    private val constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()

    override fun createPeriodicTask(context: Context, name: String): PeriodicWorkRequest {

        val request =  PeriodicWorkRequest.Builder(AppWorker::class.java, timePeriod, TimeUnit.MINUTES).apply {
            val data = Data.Builder().putString(AppWorker.SEARCH_SET_KEY, name).build()
            setInputData(data)
            setConstraints(constraints)

        }.build()
        taskList.add(request)
        return request
    }

    override fun addPeriodicTask(task: PeriodicWorkRequest): IWorkTask {
        taskList.add(task)
        return this
    }

    override fun getTaskList(): List<PeriodicWorkRequest> {
        return taskList.toList()
    }
}