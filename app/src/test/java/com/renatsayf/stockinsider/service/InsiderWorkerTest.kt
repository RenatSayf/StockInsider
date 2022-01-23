package com.renatsayf.stockinsider.service

import android.app.Application
import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class InsiderWorkerTest {

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getTargetContext()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun doWorkTest() {

        InsiderWorker.addWorkRequest("default_set")
        InsiderWorker.addWorkRequest("current_set")
        val worker = TestListenableWorkerBuilder<InsiderWorker>(Application().applicationContext).build()

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Success)

    }
}