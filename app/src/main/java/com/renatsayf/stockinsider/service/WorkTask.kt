package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import java.util.concurrent.TimeUnit


class WorkTask : IWorkTask {

    companion object {
        const val TAG = "TAG555555555"
    }

    private val taskList = mutableSetOf<PeriodicWorkRequest>()

    private val constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()

    override fun createOneTimeTask(context: Context, name: String, startTime: Long): OneTimeWorkRequest {

        val request = OneTimeWorkRequest.Builder(AppWorker::class.java).apply {
            setConstraints(constraints)
            addTag(TAG)
        }.build()

        return request
    }

    override fun createPeriodicTask(
        context: Context,
        name: String,
        timePeriod: Long
    ): PeriodicWorkRequest {

        val request =  PeriodicWorkRequest.Builder(AppWorker::class.java, timePeriod, TimeUnit.MINUTES).apply {
            val data = Data.Builder().apply {
                putString(AppWorker.SEARCH_SET_KEY, name)
            }.build()
            setInputData(data)
            setConstraints(constraints)
            addTag(TAG)
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