package com.renatsayf.stockinsider.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.AppDataBase
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.network.MockApi
import com.renatsayf.stockinsider.network.NetRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = 31)
internal class DataRepositoryImplTest {

    private lateinit var dao: AppDao
    private lateinit var db: AppDataBase
    private lateinit var repository: DataRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.searchSetDao()
        repository = DataRepositoryImpl(NetRepository(MockApi(context)), dao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveSearchSetAsync_Insert_one_record() {

        val set = RoomSearchSet(
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

        runBlocking {
            val actualId = repository.saveSearchSetAsync(set).await()

            val setByName = repository.getSearchSetFromDbAsync("XXX")

            Assert.assertEquals(setByName.id, actualId)
            Assert.assertTrue(setByName.isTracked)
        }
    }

    @Test
    fun saveSearchSetAsync_Update_record() {

        val set1 = RoomSearchSet(
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

        val set2 = RoomSearchSet(
            id = 1,
            queryName = "YYY",
            companyName = "",
            ticker = "TSLA NVDA GOOG FB NFLX",
            filingPeriod = 3,
            tradePeriod = 3,
            isPurchase = false,
            isSale = true,
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

        runBlocking {

            val id1 = repository.saveSearchSetAsync(set1).await()
            val id2 = repository.saveSearchSetAsync(set2).await()

            Assert.assertTrue(id1 == id2)
        }
    }
}