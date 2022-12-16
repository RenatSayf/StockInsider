package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import com.renatsayf.stockinsider.firebase.FireBaseViewModel
import java.util.concurrent.TimeUnit


class WorkTask(
    private val timePeriod: Long = FireBaseViewModel.workerPeriod
): IWorkTask {

    companion object {
        const val TAG = "TAG555555555"
    }

    private val taskList = mutableSetOf<PeriodicWorkRequest>()

    private val constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()

    override fun createPeriodicTask(context: Context, name: String): PeriodicWorkRequest {

        val request =  PeriodicWorkRequest.Builder(AppWorker::class.java, timePeriod, TimeUnit.MINUTES).apply {
            val data = Data.Builder().putString(AppWorker.SEARCH_SET_KEY, name).build()
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