package com.renatsayf.stockinsider.network

import android.content.Context
import com.renatsayf.stockinsider.utils.getStringFromFile
import io.reactivex.Observable
import io.reactivex.Single
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Response

class MockApi(private val context: Context): IApi {

    override fun getTradingScreen(
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
    ): Observable<Document> {
        return Observable.create { emitter ->
            val stringFromFile = context.getStringFromFile("test-data/buy_sell_deals.txt")
            val document = Jsoup.parse(stringFromFile)
            emitter.onNext(document)
        }
    }

    override fun getInsiderTrading(insiderName: String, agent: String): Single<Document> {
        return Single.create {
            val stringFromFile = context.getStringFromFile("test-data/deals_by_insider.txt")
            val document = Jsoup.parse(stringFromFile)
            it.onSuccess(document)
        }
    }

    override fun getTradingByTicker(ticker: String, agent: String): Single<Document> {
        return Single.create {
            val stringFromFile = context.getStringFromFile("test-data/deals_by_tiker.txt")
            val document = Jsoup.parse(stringFromFile)
            it.onSuccess(document)
        }
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