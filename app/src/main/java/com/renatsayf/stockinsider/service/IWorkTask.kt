package com.renatsayf.stockinsider.service

import android.content.Context
import androidx.work.PeriodicWorkRequest

interface IWorkTask {
    fun createPeriodicTask(context: Context, name: String): PeriodicWorkRequest

    fun addPeriodicTask(task: PeriodicWorkRequest): IWorkTask

    fun getTaskList(): List<PeriodicWorkRequest>
}