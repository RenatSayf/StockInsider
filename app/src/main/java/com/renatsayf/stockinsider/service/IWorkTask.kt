package com.renatsayf.stockinsider.service

import androidx.work.PeriodicWorkRequest

interface IWorkTask {
    fun createPeriodicTask(name: String): IWorkTask

    fun addPeriodicTask(task: PeriodicWorkRequest): IWorkTask

    fun getTaskList(): List<PeriodicWorkRequest>
}