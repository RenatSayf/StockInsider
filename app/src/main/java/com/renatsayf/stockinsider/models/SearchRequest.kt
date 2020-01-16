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

    private var key_tickers = "s="
    private var key_filing_date = "fd="
    private var key_trade_date = "td="
    private var key_is_purchase = "xp="
    private var key_is_sale = "xs="
    private var key_trade_min = "vl="
    private var key_trade_max = "vh="
    private var key_is_officer = "isofficer="
    private var key_is_director = "isdirector="
    private var key_is_tenpercent = "istenpercent="
    private var key_group_by = "grp="
    private var key_sort_by = "sortcol="
    private var separator = "&"

    private lateinit var paramsSet: String

    fun fetchTradingScreen()
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
                                            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())
            .subscribe({ result ->
                            val document = result
                           val body = document.body()
                           val tbody = body.select("#tablewrapper > table > tbody")
                           return@subscribe
                       }, { error ->
                           error.printStackTrace()
                       })
    }

    private fun buildRequest() : String
    {
        key_tickers = "s=$tickers"
        key_filing_date = "fd=$filingDate"
        key_trade_date = "td=".plus(tradeDate)
        key_is_purchase = "xp=".plus(isPurchase)
        key_is_sale = "xs=".plus(isSale)
        key_trade_min = "vl=".plus(tradedMin)
        key_trade_max = "vh=".plus(tradedMax)
        key_is_officer = "isofficer=".plus(isOfficer)
        key_is_director = "isdirector=".plus(isDirector)
        key_is_tenpercent = "istenpercent=".plus(isTenPercent)
        key_group_by = "grp=".plus(groupBy)
        key_sort_by = "sortcol=".plus(sortBy)

        paramsSet = "screener?$key_tickers$separator" +
                "$key_filing_date$separator" +
                "$key_trade_date$separator" +
                "$key_is_purchase$separator" +
                "$key_is_sale$separator" +
                "$key_trade_min$separator" +
                "$key_trade_max$separator" +
                "$key_is_officer$separator" +
                "$key_is_director$separator" +
                "$key_is_tenpercent$separator" +
                "$key_group_by$separator" +
                "$key_sort_by&cnt=100&page=1"

        return "http://openinsider.com/".plus(paramsSet)
    }

    fun getTradeDocument(): Document
    {
        val url = buildRequest()
        return Jsoup.connect(url).get()
    }



}