package com.renatsayf.stockinsider.ui.tracking.item

import com.renatsayf.stockinsider.db.FakeAppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.network.FakeNetRepository
import com.renatsayf.stockinsider.repository.FakeDataRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class TrackingViewModelTest {

    private lateinit var trackingVM: TrackingViewModel
    private lateinit var repository: FakeDataRepositoryImpl
    private lateinit var dao: FakeAppDao
    private var dispatcher = TestCoroutineDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {

        Dispatchers.setMain(dispatcher)
        dao = FakeAppDao()
        repository = FakeDataRepositoryImpl(
            net = FakeNetRepository(),
            dao = this.dao
        )
        trackingVM = TrackingViewModel(repository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {

        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getSearchSetById() = runTest {

        val inputSet = RoomSearchSet(
            id = 1,
            queryName = "XXX",
            companyName = "",
            ticker = "BAC MSFT AA AAPL TSLA NVDA GOOG FB NFLX",
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

        dao.setExpectedResult("", inputSet)


        val actualSet = trackingVM.getSearchSetById(1).value
        Assert.assertTrue(actualSet is RoomSearchSet)

    }
}