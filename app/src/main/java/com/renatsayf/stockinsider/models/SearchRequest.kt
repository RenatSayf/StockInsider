package com.renatsayf.stockinsider.models

import com.renatsayf.stockinsider.network.OpenInsiderService

data class SearchRequest(
        private val openInsiderService : OpenInsiderService
                        )
{
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