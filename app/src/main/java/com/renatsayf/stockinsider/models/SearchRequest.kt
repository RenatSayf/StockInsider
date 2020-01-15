package com.renatsayf.stockinsider.models

import com.renatsayf.stockinsider.network.OpenInsiderService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class SearchRequest @Inject constructor()
{
    var openInsiderService: OpenInsiderService = OpenInsiderService.create()

    var tickers: String = ""
    var filingDate: String = "0"
    var tradeDate: String = "0"
    var isPurchase: String = ""
    var isSale: String = ""
    var tradedMin: String = ""
    var tradedMax: String = ""
    var isOfficer: String = ""
    var isDirector: String = ""
    var isTenPercent: String = ""
    var groupBy: String = ""
    var sortBy: String = ""

    fun fetchTradingScreen() //: String
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
                                            )//.await()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())
            .subscribe({ result ->
                            val string = result.toString()
                       }, { error ->
                           error.printStackTrace()
                       })
    }

    fun getTradeDocument(): Document
    {
        val url = "http://openinsider.com/screener?fd=0&td=0&s=&o=&sicMin=&sicMax=&t=&minprice=&maxprice=&v=25000&sortcol=0&maxresults=500"
        return Jsoup.connect(url).get()
    }



}