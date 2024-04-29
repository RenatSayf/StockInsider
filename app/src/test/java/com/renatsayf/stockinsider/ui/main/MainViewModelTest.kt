package com.renatsayf.stockinsider.ui.main

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.renatsayf.stockinsider.db.AppDataBase
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.network.MockApi
import com.renatsayf.stockinsider.network.NetRepository
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = 31)
class MainViewModelTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule() //выполнение всех операций архитектурных компонентов в главном потоке

    private lateinit var db: AppDataBase
    private lateinit var mainVm: MainViewModel

    private val testSet = RoomSearchSet(
        queryName = "XXXXX XXX",
        companyName = "",
        ticker = "XXX",
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
        val dao = db.appDao()
        val repository = DataRepositoryImpl(NetRepository(MockApi(context), ""), dao)
        mainVm = MainViewModel(repository, null)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveSearchSet_write_not_exist_record() {

        runBlocking {
            val actualId = mainVm.saveSearchSet(testSet).value
            Assert.assertEquals(1L, actualId)
        }
    }

    @Test
    fun saveSearchSet_update_exist_record() {

        val set1 = testSet.copy().apply {
            ticker = "AAA"
            isPurchase = true
        }
        val set2 = testSet.copy().apply {
            ticker = "AAA"
            isPurchase = false
        }

        runBlocking {
            val firstId = mainVm.saveSearchSet(set1).value
            Assert.assertEquals(1L, firstId)
            val secondId = mainVm.saveSearchSet(set2).value
            Assert.assertEquals(2L, secondId)

            val list = db.appDao().getSearchSets()
            val size = list.size
            Assert.assertEquals(1, size)
        }
    }

}