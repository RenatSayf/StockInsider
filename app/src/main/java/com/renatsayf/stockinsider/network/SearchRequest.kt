package com.renatsayf.stockinsider.network

import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject

class SearchRequest @Inject constructor()
{
    private var openInsiderService: OpenInsiderService = OpenInsiderService.create()
    private var disposable : Disposable? = null
    private var searchTicker : String = ""

    companion object
    {
        interface IDocumentListener
        {
            fun onDocumentError(throwable : Throwable)
            fun onDealListReady(dealList : ArrayList<Deal>)
        }
        private var documentListener: IDocumentListener? = null
    }

    fun setOnDocumentReadyListener(iDocumentListener : IDocumentListener)
    {
        documentListener =  iDocumentListener
    }

    fun getTradingScreen(set : SearchSet)
    {
        var dealList : ArrayList<Deal> = arrayListOf()
        searchTicker = set.ticker
        disposable = openInsiderService.getInsiderTrading(
                set.ticker,
                set.filingPeriod,
                set.tradePeriod,
                set.isPurchase,
                set.isSale,
                set.tradedMin,
                set.tradedMax,
                set.isOfficer,
                set.isOfficer,
                set.isOfficer,
                set.isOfficer,
                set.isOfficer,
                set.isOfficer,
                set.isOfficer,
                set.isOfficer,
                set.isDirector,
                set.isTenPercent,
                set.groupBy,
                set.sortBy
                                                         )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())
            .subscribe({ document ->
                           dealList = doParseDocument(document)
                           documentListener?.onDealListReady(dealList)
                           disposable?.dispose()
                           return@subscribe
                       }, { error : Throwable ->
                           error.runCatching {
                               if (error is IndexOutOfBoundsException)
                               {
                                   documentListener?.onDealListReady(dealList)
                                   disposable?.dispose()
                               }
                               else
                               {
                                   documentListener?.onDocumentError(error)
                                   disposable?.dispose()
                               }
                           }
                       })
    }

    private fun doParseDocument(document : Document) : ArrayList<Deal>
    {
        val listDeal : ArrayList<Deal> = arrayListOf()
        val body = document.body()
        val bodyText = body.text()
        if (bodyText != null && bodyText.contains("ERROR:"))
        {
            val deal = Deal("")
            deal.error = "Data is not available, please, try later"
            listDeal.add(deal)
            return listDeal
        }
        val error = body?.select("#tablewrapper")?.text()
        if (error != null && error.contains("ERROR:"))
        {
            val deal = Deal("")
            deal.error = "Data is not available, please, try later"
            listDeal.add(deal)
            return listDeal
        }
        val tableBody = body?.select("#tablewrapper > table > tbody")

        val table = tableBody?.get(0)
        var trIndex = 1
        table?.children()?.forEach { element: Element? ->
            val filingDate = element?.select("tr:nth-child($trIndex) > td:nth-child(2) > div > a")?.text()
            val deal = Deal(filingDate)
            deal.filingDateRefer = element?.select("tr:nth-child($trIndex) > td:nth-child(2) > div > a")?.attr("href")
            deal.tradeDate = element?.select("tr:nth-child($trIndex) > td:nth-child(3) > div")?.text()
            deal.ticker = element?.select("tr:nth-child($trIndex) > td:nth-child(4) > b > a")?.text()
            val tdIndex = if(searchTicker == "") 0 else -1
            deal.company = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 5}) > a")?.text()
            deal.insiderName = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 6}) > a")?.text()
            deal.insiderNameRefer = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 6}) > a")?.attr("href")
            deal.insiderTitle = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 7})")?.text()
            deal.tradeType = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 8})")?.text()
            deal.price = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 9})")?.text()
            deal.volumeStr = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 13})")?.text().toString()
            listDeal.add(deal)
            trIndex++
        }
        return listDeal
    }



}