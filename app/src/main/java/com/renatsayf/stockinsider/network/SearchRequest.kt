package com.renatsayf.stockinsider.network

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class SearchRequest @Inject constructor()
{
    private var openInsiderService: OpenInsiderService = OpenInsiderService.create()

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

    private var keyTickers = "s="
    private var keyFilingDate = "fd="
    private var keyTradeDate = "td="
    private var keyIsPurchase = "xp="
    private var keyIsSale = "xs="
    private var keyTradeMin = "vl="
    private var keyTradeMax = "vh="
    private var keyIsOfficer = "isofficer="
    private var keyIsDirector = "isdirector="
    private var keyIsTenPercent = "istenpercent="
    private var keyGroupBy = "grp="
    private var keySortBy = "sortcol="
    private var separator = "&"

    private lateinit var paramsSet: String
    private var disposable : Disposable? = null

    companion object
    {
        interface IDocumentListener
        {
            fun onDocumentReady(document : Document)
            fun onDocumentError(throwable : Throwable)
        }
        private var documentListener: IDocumentListener? = null
    }

    fun setOnDocumentReadyListener(iDocumentListener : IDocumentListener)
    {
        documentListener =  iDocumentListener
    }

    fun fetchTradingScreen()
    {
        disposable = openInsiderService.getInsiderTrading(
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
                            documentListener?.onDocumentReady(result)
                           return@subscribe
                       }, { error : Throwable ->
                           error.runCatching {
                               if (error is IndexOutOfBoundsException)
                               {
                                   documentListener?.onDocumentReady(Document(""))
                               }
                               else
                               {
                                   documentListener?.onDocumentError(error)
                               }
                           }
                       })
    }

    private fun buildRequest() : String
    {
        keyTickers = "s=$tickers"
        keyFilingDate = "fd=$filingDate"
        keyTradeDate = "td=".plus(tradeDate)
        keyIsPurchase = "xp=".plus(isPurchase)
        keyIsSale = "xs=".plus(isSale)
        keyTradeMin = "vl=".plus(tradedMin)
        keyTradeMax = "vh=".plus(tradedMax)
        keyIsOfficer = "isofficer=".plus(isOfficer)
        keyIsDirector = "isdirector=".plus(isDirector)
        keyIsTenPercent = "istenpercent=".plus(isTenPercent)
        keyGroupBy = "grp=".plus(groupBy)
        keySortBy = "sortcol=".plus(sortBy)

        paramsSet = "screener?$keyTickers$separator" +
                "$keyFilingDate$separator" +
                "$keyTradeDate$separator" +
                "$keyIsPurchase$separator" +
                "$keyIsSale$separator" +
                "$keyTradeMin$separator" +
                "$keyTradeMax$separator" +
                "$keyIsOfficer$separator" +
                "$keyIsDirector$separator" +
                "$keyIsTenPercent$separator" +
                "$keyGroupBy$separator" +
                "$keySortBy&cnt=100&page=1"

        return "http://openinsider.com/".plus(paramsSet)
    }

    fun getTradeDocument(): Document
    {
        val url = buildRequest()
        return Jsoup.connect(url).get()
    }



}