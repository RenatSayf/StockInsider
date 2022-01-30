package com.renatsayf.stockinsider.service

import androidx.work.PeriodicWorkRequest

interface IWorkTask {
    fun createPeriodicTask(name: String): PeriodicWorkRequest

    fun addPeriodicTask(task: PeriodicWorkRequest)

    fun getTaskList(): List<PeriodicWorkRequest>
}