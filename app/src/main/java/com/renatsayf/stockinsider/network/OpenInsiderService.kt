package com.renatsayf.stockinsider.network

import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface OpenInsiderService
{
    @Headers(
            "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36"
            )
    @GET("screener")
    fun getInsiderTrading(
                @Query("s", encoded = true) ticker: String,
                @Query("fd") filingDate: String,
                @Query("td") tradeDate: String,
                @Query("xp") isPurchase: String,
                @Query("xs") isSale: String,
                @Query("vl") tradedMin: String,
                @Query("vh") tradedMax: String,
                @Query("isofficer") isOfficer: String,
                @Query("iscob") isCob : String = isOfficer,
                @Query("isceo") isSeo: String = isOfficer,
                @Query("ispres") isPres: String = isOfficer,
                @Query("iscoo") isCoo: String = isOfficer,
                @Query("iscfo") isCfo: String = isOfficer,
                @Query("isgc") isGc: String = isOfficer,
                @Query("isvp") isVp: String = isOfficer,
                @Query("isdirector") isDirector: String,
                @Query("istenpercent") isTenPercent: String,
                @Query("grp") groupBy: String,
                @Query("sortcol") sortBy: String
                         ): Observable<Document>

    companion object Factory
    {
        fun create() : OpenInsiderService
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

            return retrofit.create(OpenInsiderService::class.java)
        }

    }
}