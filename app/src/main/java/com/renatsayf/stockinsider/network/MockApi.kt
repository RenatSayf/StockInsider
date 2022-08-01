package com.renatsayf.stockinsider.network

import android.content.Context
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.utils.getStringFromFile
import io.reactivex.Observable
import io.reactivex.Single
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

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
        isCob: String,
        isSeo: String,
        isPres: String,
        isCoo: String,
        isCfo: String,
        isGc: String,
        isVp: String,
        isDirector: String,
        isTenPercent: String,
        groupBy: String,
        sortBy: String
    ): Observable<Document> {
        return Observable.create { emitter ->
            val stringFromFile = context.getStringFromFile("test-data/buy_sell_deals.txt")
            val document = Jsoup.parse(stringFromFile)
            emitter.onNext(document)
        }
    }

    override fun getInsiderTrading(insiderName: String): Single<Document> {
        return Single.create {
            val stringFromFile = context.getStringFromFile("test-data/deals_by_insider.txt")
            val document = Jsoup.parse(stringFromFile)
            it.onSuccess(document)
        }
    }

    override fun getTradingByTicker(ticker: String): Single<Document> {
        return Single.create {
            val stringFromFile = context.getStringFromFile("test-data/deals_by_tiker.txt")
            val document = Jsoup.parse(stringFromFile)
            it.onSuccess(document)
        }
    }
}