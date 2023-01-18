package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.work.*
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.firebase.FireBaseViewModel
import com.renatsayf.stockinsider.utils.timeToFormattedString
import java.util.*
import java.util.concurrent.TimeUnit


class WorkTask(
    private val timePeriod: Long = try {
        FireBaseViewModel.workerPeriod
    }
    catch (e: ExceptionInInitializerError) {
        20L
    }
    catch (e: NoClassDefFoundError) {
        20L
    }
    catch (e: Exception) {
        20L
    }
): IWorkTask {

    companion object {
        const val TAG = "TAG555555555"
    }

    private val taskList = mutableSetOf<PeriodicWorkRequest>()

    private val constraints = Constraints.Builder().apply {
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()

    override fun createOneTimeTask(context: Context, name: String, startTime: Long): OneTimeWorkRequest {

        val currentTimeInMillis = Calendar.getInstance().apply {
            timeInMillis += TimeZone.getDefault().rawOffset
        }.timeInMillis
        val delay = TimeUnit.MILLISECONDS.toMinutes(startTime - currentTimeInMillis)

        val request = OneTimeWorkRequest.Builder(AppWorker::class.java).apply {
            setInitialDelay(delay, TimeUnit.MINUTES)
            setConstraints(constraints)
            addTag(TAG)
        }.build()

        if (BuildConfig.DEBUG) {
            println("******************** ${this::class.java.simpleName}.createOneTimeTask() currentTime: ${currentTimeInMillis.timeToFormattedString()} ***********************")
            println("******************** ${this::class.java.simpleName}.createOneTimeTask() startTime: ${startTime.timeToFormattedString()} ***********************")
            println("******************** ${this::class.java.simpleName}.createOneTimeTask() delay : $delay *********************")
        }

        return request
    }

    override fun createPeriodicTask(context: Context, name: String): PeriodicWorkRequest {

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