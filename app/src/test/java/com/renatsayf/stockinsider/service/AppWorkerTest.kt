package com.renatsayf.stockinsider.service

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.TestWorkerBuilder
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.FakeAppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.network.FakeNetworkRepository
import io.reactivex.Observable
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.Executors


@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = 30, manifest = Config.NONE)
//@RunWith(AndroidJUnit4::class)
class AppWorkerTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule() //выполнение всех операций архитектурных компонентов в главном потоке

    private lateinit var context: Context
    private lateinit var config: Configuration

    private lateinit var db: AppDao

    @Before
    fun setUp() {

        context = ApplicationProvider.getApplicationContext()

        db = FakeAppDao()

        config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
    }

    @After
    fun tearDown() {

    }

    @Test
    fun workManagerEnqueueTest() {

        val task = WorkTask().apply {
            createPeriodicTask("Microsoft")
        }

        val actualWorkRequests = task.getTaskList()
        assertTrue(actualWorkRequests.isNotEmpty())

        val workManager = WorkManager.getInstance(context)

        workManager.enqueue(actualWorkRequests).result.get()

        val workInfo = workManager.getWorkInfoById(actualWorkRequests[0].id).get()

        assertEquals(WorkInfo.State.ENQUEUED, workInfo.state)
    }

    @Test
    fun doWorkTest() {

        val network = FakeNetworkRepository()
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
        )

        (db as FakeAppDao).setExpectedResult("Microsoft", roomSearchSet)

        val worker = TestWorkerBuilder<AppWorker>(
            context,
            Executors.newSingleThreadExecutor(),
            inputData = Data(workDataOf(AppWorker.SEARCH_SET_KEY to "Microsoft"))
        ).build()

        worker.injectDependencies(db, network)

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Success)

    }
}

