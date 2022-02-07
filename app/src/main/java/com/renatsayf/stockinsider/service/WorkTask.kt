package com.renatsayf.stockinsider.service

import androidx.annotation.RestrictTo
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import com.renatsayf.stockinsider.BuildConfig
import java.util.concurrent.TimeUnit


object WorkTask: IWorkTask {

    private val timePeriod = if(BuildConfig.DEBUG) 20L else 480L

    private val taskList = mutableSetOf<PeriodicWorkRequest>()

    private val constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()

    override fun createPeriodicTask(name: String): IWorkTask {
        val request =  PeriodicWorkRequest.Builder(AppWorker::class.java, timePeriod, TimeUnit.MINUTES).apply {

            val data = Data.Builder().putString(AppWorker.SEARCH_SET_KEY, name).build()
            setInputData(data)
            setConstraints(constraints)

        }.build()
        taskList.add(request)
        return this
    }

    override fun addPeriodicTask(task: PeriodicWorkRequest): IWorkTask {
        taskList.add(task)
        return this
    }

    override fun getTaskList(): List<PeriodicWorkRequest> {
        return taskList.toList()
    }
}