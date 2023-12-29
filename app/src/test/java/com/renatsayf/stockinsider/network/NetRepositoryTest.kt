package com.renatsayf.stockinsider.network

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.utils.getStringFromFile
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.jsoup.Jsoup
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit


@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = 30)
class NetRepositoryTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule() //выполнение всех операций архитектурных компонентов в главном потоке

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val server = MockWebServer()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    private val repository = NetRepository(createRetrofit())

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
    }

    @After
    fun tearDown() {
    }

    private fun createRetrofit(): IApi {
        return Retrofit.Builder()
            .addConverterFactory(DocumAdapter.FACTORY)
            .baseUrl(server.url("/"))
            .client(okHttpClient)
            .build()
            .create(IApi::class.java)
    }

    @Test
    fun doAllCompanyNameParsing_norm() {

        val fromFile = context.getStringFromFile("test-data/buy_sell_deals.txt")
        val document = Jsoup.parse(fromFile)

        val actualSet = repository.doAllCompanyNameParsing(document)
        Assert.assertTrue(actualSet.isNotEmpty())
    }

    @Test
    fun doAllCompanyNameParsing_wrong_data() {

        val fromFile = context.getStringFromFile("test-data/deals_by_insider.txt")
        val document = Jsoup.parse(fromFile)

        val actualSet = repository.doAllCompanyNameParsing(document)
        Assert.assertTrue(actualSet.isEmpty())
    }

    @Test
    fun getAllCompaniesName_valid_html() {
        val fromFile = context.getStringFromFile("test-data/buy_sell_deals.txt")

        server.apply {
            val response = MockResponse().apply {
                setResponseCode(200)
                setBody(fromFile)
            }
            this.enqueue(response)
        }

        runBlocking {
            val result = repository.getAllCompaniesNameAsync().await()
            result.onSuccess { lict ->
                Assert.assertTrue(lict.isNotEmpty() && lict.size > 50)
            }
            result.onFailure {
                Assert.assertTrue(false)
            }
        }
    }

    @Test
    fun doMainParsing() {
        val fromFile = context.getStringFromFile("test-data/buy-all-5000K$.txt")
        val document = Jsoup.parse(fromFile)

        val actualList = repository.doMainParsing(document)
        Assert.assertTrue(actualList.isNotEmpty())
    }

    @Test
    fun getTradingScreen() {
        val fromFile = context.getStringFromFile("test-data/buy-all-5000K$.txt")

        server.apply {
            val response = MockResponse().apply {
                setResponseCode(200)
                setBody(fromFile)
            }
            this.enqueue(response)
        }
        runBlocking {
            val actualList = repository.getDealsListAsync(testSet.toSearchSet()).await()
            Assert.assertTrue(actualList.isNotEmpty())
        }
    }

    @Test
    fun getDealsListAsync_200Ok() {
        val fromFile = context.getStringFromFile("test-data/buy-all-5000K$.txt")

        server.apply {
            val response = MockResponse().apply {
                setResponseCode(200)
                setBody(fromFile)
            }
            this.enqueue(response)
        }
        runBlocking {
            val actualResult = repository.getDealsListAsync(testSet.toSearchSet()).await()
            Assert.assertEquals(24, actualResult.size)
        }
    }
}