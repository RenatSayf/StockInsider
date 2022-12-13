package com.renatsayf.stockinsider.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.AppDataBase
import com.renatsayf.stockinsider.db.Company
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

    private val testSet = RoomSearchSet(
        queryName = "XXX",
        companyName = "",
        ticker = "BAC MSFT AA AAPL TSLA NVDA GOOG FB NFLX",
        filingPeriod = 3,
        tradePeriod = 3,
        isPurchase = true,
        isSale = false,
        tradedMin = "",
        isDirector = true,
        isTenPercent = true,
        tradedMax = "",
        isOfficer = true,
        groupBy = 1,
        sortBy = 3
    ).apply {
        isTracked = true
    }

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
            isDirector = true,
            isTenPercent = true,
            tradedMax = "",
            isOfficer = true,
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

            val list = repository.getUserSearchSetsFromDbAsync()
            val actualSet = list.first {
                it.id == id2
            }

            Assert.assertEquals(set2.queryName, actualSet.queryName)
            Assert.assertEquals(set2.ticker, actualSet.ticker)
            Assert.assertEquals(set2.isTracked, actualSet.isTracked)
        }
    }

    @Test
    fun getSearchSetByIdAsync_actual_id() {

        val expectedSet = RoomSearchSet(
            id = 0,
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

            val actualId = repository.saveSearchSetAsync(expectedSet).await()
            Assert.assertTrue(actualId > 0)

            val actualSet = repository.getSearchSetByIdAsync(actualId).await()
            val isEquals = expectedSet == actualSet
            Assert.assertTrue(isEquals)
        }
    }

    @Test
    fun getSearchSetByIdAsync_not_exist_id() {

        runBlocking {

            val notExistId = 999L
            val actualSet = repository.getSearchSetByIdAsync(notExistId).await()
            Assert.assertTrue(actualSet == null)
        }
    }

    @Test
    fun getAllSimilar() {

        val pattern1 = "tes"
        val pattern2 = "mi"
        val pattern3 = "app"

        val companies = listOf(
            Company("MSFT", "Microsoft Corp."),
            Company("TSLA", "Tesla, Inc."),
            Company("AAPL", "Apple Inc.")
        )

        runBlocking {
            repository.insertCompanies(companies)

            val actualList = repository.getCompaniesFromDbAsync()
            Assert.assertTrue(actualList?.size == 3)

            var actualSimilar = repository.getAllSimilar(pattern1)
            val actualTicker1 = actualSimilar[0].ticker
            Assert.assertTrue(actualTicker1 == "TSLA")

            actualSimilar = repository.getAllSimilar(pattern2)
            val actualTicker2 = actualSimilar[0].ticker
            Assert.assertTrue(actualTicker2 == "MSFT")

            actualSimilar = repository.getAllSimilar(pattern3)
            val actualTicker3 = actualSimilar[0].ticker
            Assert.assertTrue(actualTicker3 == "AAPL")

        }
    }

    @Test
    fun deleteSetById_actual_id(){
        val expectedSet = RoomSearchSet(
            id = 0,
            queryName = "XXX",
            companyName = "",
            ticker = "YYYY",
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

            val actualId = repository.saveSearchSetAsync(expectedSet).await()
            Assert.assertTrue(actualId > 0)

            val actualSet = repository.getSearchSetByIdAsync(actualId).await()
            Assert.assertTrue(actualSet?.id == actualId)

            val notExistId = 999L
            var actualResult = repository.deleteSetByIdAsync(notExistId).await()
            Assert.assertTrue(actualResult == 0)

            actualResult = repository.deleteSetByIdAsync(actualId).await()
            Assert.assertTrue(actualResult > 0)
        }
    }

    @Test
    fun getTrackedCount() {

        val set1 = testSet.copy(queryName = "query 1", ticker = "SSS").apply {
            target = com.renatsayf.stockinsider.models.Target.Tracking
            isTracked = true
        }

        runBlocking {
            val id = repository.saveSearchSetAsync(set1).await()
            var actualCount = repository.getTrackedCountAsync().await()
            Assert.assertTrue(actualCount == 1)

            repository.deleteSetByIdAsync(id).await()

            actualCount = repository.getTrackedCountAsync().await()
            Assert.assertTrue(actualCount == 0)
        }

    }

    @Test
    fun insertCompanies() {

        val companies = listOf(
            Company("AAA", "AAAAAAAAA"),
            Company("BBB", "BBBBBBBBBBBBBB"),
            Company("CCC", "CCCCCCCCCCC CCCCC")
        )

        runBlocking {

            repository.insertCompanies(companies)
            val actualList = repository.getCompaniesFromDbAsync()
            Assert.assertTrue(actualList?.size == 3)
        }
    }
}





















