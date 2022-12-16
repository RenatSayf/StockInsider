package com.renatsayf.stockinsider.service

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.AppDataBase
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.network.FakeNetRepository
import io.reactivex.Observable
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = 30, manifest = Config.NONE)
class AppWorkerTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule() //выполнение всех операций архитектурных компонентов в главном потоке

    private lateinit var context: Context
    private lateinit var config: Configuration

    private lateinit var dao: AppDao
    private lateinit var db: AppDataBase

    @Before
    fun setUp() {

        context = ApplicationProvider.getApplicationContext()

        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.searchSetDao()

        config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

//    @Test
//    fun workManagerEnqueueTest() {
//
//        val workTask = WorkTask(15L)
//
//        val actualWorkRequests = workTask.getTaskList()
//        assertTrue(actualWorkRequests.isNotEmpty())
//
//        val workManager = WorkManager.getInstance(context)
//
//        workManager.enqueue(actualWorkRequests).result.get()
//
//        val workInfo = workManager.getWorkInfoById(actualWorkRequests[0].id).get()
//
//        assertEquals(WorkInfo.State.ENQUEUED, workInfo.state)
//    }

    @Test
    fun doWorkTest() {

        val network = FakeNetRepository()
        val deal = Deal("18.12.2022").apply {
            ticker = "MSFT"
        }
        val expectedDealList = Observable.fromArray(arrayListOf(deal))
        network.setExpectedResult(expectedDealList)

        val roomSearchSet = RoomSearchSet(
            queryName = "Microsoft",
            companyName = "Microsoft",
            ticker = "MSFT",
            filingPeriod = 3,
            tradePeriod = 3,
            tradedMin = "",
            tradedMax = "",
            isOfficer = true,
            isDirector = true,
            isTenPercent = true,
            groupBy = 0,
            sortBy = 0
        ).apply {
            target = com.renatsayf.stockinsider.models.Target.Tracking
            isTracked = true
        }

        val worker = TestListenableWorkerBuilder<AppWorker>(context).build()

        worker.injectDependencies(
            dao,
            network,
            "",
            FakeServiceNotification.notify
        )

        runBlocking{

            dao.insertOrUpdateSearchSet(roomSearchSet)

            val result = worker.doWork()
            val result2 = worker.doWork()
            assertTrue(result is ListenableWorker.Result.Success)
            assertTrue(result2 is ListenableWorker.Result.Success)
        }
    }
}

