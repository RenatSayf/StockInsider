package com.renatsayf.stockinsider.network

import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface IApi
{
    @Headers(
            userAgentHeader,
            "Content-Type:application/x-www-form-urlencoded; charset=utf-8"
            )
    @GET("screener")
    fun getTradingScreen(
                @Query("s", encoded = true) ticker: String,
                @Query("fd", encoded = true) filingDate: String,
                @Query("td", encoded = true) tradeDate: String,
                @Query("xp", encoded = true) isPurchase: String,
                @Query("xs", encoded = true) isSale: String,
                @Query("excludeDerivRelated", encoded = true) excludeDerivRelated: String = "1",
                @Query("vl", encoded = true) tradedMin: String,
                @Query("vh", encoded = true) tradedMax: String,
                @Query("isofficer", encoded = true) isOfficer: String,
                @Query("iscob", encoded = true) isCob : String = isOfficer,
                @Query("isceo", encoded = true) isSeo: String = isOfficer,
                @Query("ispres", encoded = true) isPres: String = isOfficer,
                @Query("iscoo", encoded = true) isCoo: String = isOfficer,
                @Query("iscfo", encoded = true) isCfo: String = isOfficer,
                @Query("isgc", encoded = true) isGc: String = isOfficer,
                @Query("isvp", encoded = true) isVp: String = isOfficer,
                @Query("isdirector", encoded = true) isDirector: String,
                @Query("istenpercent", encoded = true) isTenPercent: String,
                @Query("grp", encoded = true) groupBy: String,
                @Query("sortcol", encoded = true) sortBy: String
                         ): Observable<Document>

    @Headers(userAgentHeader)
    @GET("{insiderName}")
    fun getInsiderTrading(@Path("insiderName", encoded = true) insiderName: String): Single<Document>

    @Headers(userAgentHeader)
    @GET("{ticker}")
    fun getTradingByTicker(@Path("ticker", encoded = true) ticker: String): Single<Document>

    companion object Factory
    {
        private const val userAgentHeader = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36"

        fun create() : IApi
        {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC

            val okHttpClient = OkHttpClient().newBuilder()
                .addInterceptor(interceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS).build()

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(DocumAdapter.FACTORY)
                .baseUrl("http://openinsider.com/")
                .client(okHttpClient).build()

            return retrofit.create(IApi::class.java)
        }

    }
}