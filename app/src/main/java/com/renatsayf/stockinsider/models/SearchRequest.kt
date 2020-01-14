package com.renatsayf.stockinsider.models

import com.renatsayf.stockinsider.network.OpenInsiderService
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class SearchRequest @Inject constructor()
{
    var openInsiderService: OpenInsiderService
    init
    {
        openInsiderService = OpenInsiderService.invoke(Interceptor {
            chain -> Response.Builder().build()
        })
    }

    lateinit var tickers: String
    lateinit var filingDate: String
    lateinit var tradeDate: String
    lateinit var isPurchase: String
    lateinit var isSale: String
    lateinit var tradedMin: String
    lateinit var tradedMax: String
    lateinit var isOfficer: String
    lateinit var isDirector: String
    lateinit var isTenPercent: String
    lateinit var groupBy: String
    lateinit var sortBy: String

    suspend fun fetchTradingScreen() : String
    {
        val res = openInsiderService.getInsiderTrading(
                tickers,
                filingDate,
                tradeDate,
                isPurchase,
                isSale,
                tradedMin,
                tradedMax,
                isOfficer,
                isOfficer,
                isOfficer,
                isOfficer,
                isOfficer,
                isOfficer,
                isOfficer,
                isOfficer,
                isDirector,
                isTenPercent,
                groupBy,
                sortBy
                                            ).await()
        return res
    }
}