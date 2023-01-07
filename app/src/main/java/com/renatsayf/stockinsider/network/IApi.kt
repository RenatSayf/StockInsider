package com.renatsayf.stockinsider.network

import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Deferred
import okhttp3.Response
import org.jsoup.nodes.Document
import retrofit2.http.*

interface IApi
{
    @Headers(
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
                @Query("isofficer", encoded = false) isOfficer: String,
                @Query("isdirector", encoded = true) isDirector: String,
                @Query("istenpercent", encoded = true) isTenPercent: String,
                @Query("grp", encoded = true) groupBy: String,
                @Query("sortcol", encoded = true) sortBy: String,
                @Query("cnt", encoded = true) maxResult: String = "500",
                @Header("User-Agent") agent: String,
                         ): Observable<Document>

    @GET("{insiderName}")
    fun getInsiderTrading(
        @Path("insiderName", encoded = true) insiderName: String,
        @Header("User-Agent") agent: String
    ): Single<Document>

    @GET("{ticker}")
    fun getTradingByTicker(
        @Path("ticker", encoded = true) ticker: String,
        @Header("User-Agent") agent: String
    ): Single<Document>

    @Headers(
        "Content-Type:application/x-www-form-urlencoded; charset=utf-8"
    )
    @GET("screener")
    suspend fun getDealsListAsync(
        @Query("s", encoded = true) ticker: String,
        @Query("fd", encoded = true) filingDate: String,
        @Query("td", encoded = true) tradeDate: String,
        @Query("xp", encoded = true) isPurchase: String,
        @Query("xs", encoded = true) isSale: String,
        @Query("excludeDerivRelated", encoded = true) excludeDerivRelated: String = "1",
        @Query("vl", encoded = true) tradedMin: String,
        @Query("vh", encoded = true) tradedMax: String,
        @Query("isofficer", encoded = false) isOfficer: String,
        @Query("isdirector", encoded = true) isDirector: String,
        @Query("istenpercent", encoded = true) isTenPercent: String,
        @Query("grp", encoded = true) groupBy: String,
        @Query("sortcol", encoded = true) sortBy: String,
        @Query("cnt", encoded = true) maxResult: String = "500",
        @Header("User-Agent") agent: String,
    ): retrofit2.Response<Document>

}