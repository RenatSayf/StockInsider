package com.renatsayf.stockinsider.ui.tracking.item

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.AppDataBase
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.network.MockApi
import com.renatsayf.stockinsider.network.NetRepository
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import com.renatsayf.stockinsider.ui.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = 31)
class TrackingViewModelTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule() //выполнение всех операций архитектурных компонентов в главном потоке

    private lateinit var trackingVM: TrackingViewModel
    private lateinit var db: AppDataBase
    private lateinit var dao: AppDao
    private lateinit var repository: DataRepositoryImpl
    private var dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {

        Dispatchers.setMain(dispatcher)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.appDao()

        repository = DataRepositoryImpl(NetRepository(MockApi(context)), dao)
        trackingVM = TrackingViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getSearchSetById() = runBlocking {

        val inputSet = RoomSearchSet(
            id = 1,
            queryName = "XXXXX XXX",
            companyName = "",
            ticker = "XXX",
            filingPeriod = 3,
            tradePeriod = 3,
            isPurchase = true,
            isSale = false,
            tradedMin = "",
            tradedMax = "",
            isOfficer = true,
            isDirector = true,
            isTenPercent = true,
            groupBy = 1,
            sortBy = 3
        ).apply {
            isTracked = true
        }

        MainViewModel(repository, null).saveSearchSet(inputSet)

        val actualSet = trackingVM.getSearchSetById(1).value
        Assert.assertEquals(inputSet, actualSet)
    }
}