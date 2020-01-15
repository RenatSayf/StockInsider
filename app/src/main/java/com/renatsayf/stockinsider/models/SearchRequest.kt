package com.renatsayf.stockinsider.models

import com.renatsayf.stockinsider.network.OpenInsiderService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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

        //return res.toString()



}