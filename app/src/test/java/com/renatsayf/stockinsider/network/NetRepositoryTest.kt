package com.renatsayf.stockinsider.network

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.utils.getStringFromFile
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.jsoup.Jsoup
import org.junit.*

import org.junit.Assert
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit


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

    private lateinit var testScheduler: TestScheduler

    @Before
    fun setUp() {
        testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler {
            testScheduler
        }
    }

    @After
    fun tearDown() {
        RxJavaPlugins.setComputationSchedulerHandler { null }
    }

    private fun createRetrofit(): IApi {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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
        val testObserver = TestObserver<List<Company>>()

        val single = repository.getAllCompaniesName()
        single.subscribe(testObserver)
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        val actualList = testObserver.values()[0]

        Assert.assertTrue(actualList.isNotEmpty() && actualList.size > 50)
    }
}