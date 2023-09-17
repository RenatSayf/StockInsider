package com.renatsayf.stockinsider.network

import android.content.Context
import com.renatsayf.stockinsider.utils.getStringFromFile
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Response

class MockApi(private val context: Context): IApi {

    override suspend fun getInsiderTradingAsync(
        insiderName: String,
        agent: String
    ): Response<Document> {
        val stringFromFile = context.getStringFromFile("test-data/deals_by_insider.txt")
        val document = Jsoup.parse(stringFromFile)
        return Response.success(document)
    }

    override suspend fun getDealsByTicker(ticker: String, agent: String): Response<Document> {
        val stringFromFile = context.getStringFromFile("test-data/deals_by_tiker.txt")
        val document = Jsoup.parse(stringFromFile)
        return Response.success(document)
    }

    override suspend fun getDealsListAsync(
        ticker: String,
        filingDate: String,
        tradeDate: String,
        isPurchase: String,
        isSale: String,
        excludeDerivRelated: String,
        tradedMin: String,
        tradedMax: String,
        isOfficer: String,
        isDirector: String,
        isTenPercent: String,
        groupBy: String,
        sortBy: String,
        maxResult: String,
        agent: String
    ): Response<Document> {
        val stringFromFile = context.getStringFromFile("test-data/buy_sell_deals.txt")
        val document = Jsoup.parse(stringFromFile)
        return Response.success(document)
    }
}