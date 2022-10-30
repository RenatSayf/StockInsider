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
@Config(maxSdk = 30)
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
    fun saveSearchSetAsync() {

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
}