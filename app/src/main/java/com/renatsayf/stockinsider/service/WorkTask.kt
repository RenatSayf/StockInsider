package com.renatsayf.stockinsider.service

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import java.util.concurrent.TimeUnit

class WorkTask: IWorkTask {

    private val taskList = mutableSetOf<PeriodicWorkRequest>()

    private val constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()

    override fun createPeriodicTask(name: String): PeriodicWorkRequest {
        val request =  PeriodicWorkRequest.Builder(AppWorker::class.java, 15, TimeUnit.MINUTES).apply {
            val data = Data.Builder().putString(AppWorker.SEARCH_SET_KEY, name).build()
            setInputData(data)
            setConstraints(constraints)

        }.build()
        taskList.add(request)
        return request
    }

    override fun addPeriodicTask(task: PeriodicWorkRequest) {
        taskList.add(task)
    }

    override fun getTaskList(): List<PeriodicWorkRequest> {
        return taskList.toList()
    }
}