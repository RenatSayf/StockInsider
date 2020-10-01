package com.renatsayf.stockinsider.network

import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject

class SearchRequest @Inject constructor()
{
    private var openInsiderService: OpenInsiderService = OpenInsiderService.create()
    private var composite = CompositeDisposable()
    private var searchTicker : String = ""

    fun getTradingScreen(set: SearchSet) : io.reactivex.Observable<ArrayList<Deal>>
    {
        return io.reactivex.Observable.create {emitter ->
            var dealList: ArrayList<Deal> = arrayListOf()
            searchTicker = set.ticker
            val subscriber = openInsiderService.getTradingScreen(
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
                .map { document ->
                    dealList = doMainParsing(document)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe({
                    emitter.onNext(dealList)
                }, { error: Throwable ->
                    emitter.onError(error)
                }, {
                    emitter.onComplete()
                })
            composite.add(subscriber)
        }
    }

    private fun doMainParsing(document : Document) : ArrayList<Deal>
    {
        val listDeal : ArrayList<Deal> = arrayListOf()
        val body = document.body()
        val bodyText = body.text()
        if (bodyText != null && bodyText.contains("ERROR:"))
        {
            val deal = Deal("")
            deal.error = "ERROR:"
            listDeal.add(deal)
            return listDeal
        }
        val error = body?.select("#tablewrapper")?.text()
        if (error != null && error.contains("ERROR:"))
        {
            val deal = Deal("")
            deal.error = "ERROR:"
            listDeal.add(deal)
            return listDeal
        }
        val tableBody = body?.select("#tablewrapper > table > tbody")
        tableBody?.let { it ->
            if (it.size > 0)
            {
                val table = it[0]
                var trIndex = 1
                table?.children()?.forEach { element: Element? ->
                    val filingDate = element?.select("tr:nth-child($trIndex) > td:nth-child(2) > div > a")?.text()
                    val deal = Deal(filingDate)
                    deal.filingDateRefer = element?.select("tr:nth-child($trIndex) > td:nth-child(2) > div > a")?.attr("href")
                    deal.tradeDate = element?.select("tr:nth-child($trIndex) > td:nth-child(3) > div")?.text()
                    deal.ticker = element?.select("tr:nth-child($trIndex) > td:nth-child(4) > b > a")?.text()
                    val tdIndex = when
                    {
                        searchTicker.isNotEmpty() && !searchTicker.contains("+") ->
                        {
                            -1
                        }
                        else -> 0
                    }
                    deal.company = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 5}) > a")?.text()
                    deal.insiderName = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 6}) > a")?.text()
                    deal.insiderNameRefer = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 6}) > a")?.attr("href")
                    deal.insiderTitle = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 7})")?.text()
                    deal.tradeType = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 8})")?.text()
                    deal.price = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 9})")?.text()
                    deal.qty = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 10})")?.text()
                    deal.owned = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 11})")?.text()
                    deal.deltaOwn = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 12})")?.text()
                    deal.volumeStr = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 13})")?.text().toString()
                    listDeal.add(deal)
                    trIndex++
                }
            }
        }
        return listDeal
    }

    fun getInsiderTrading(insider: String): Single<ArrayList<Deal>>
    {
        return Single.create { emitter ->
            var dealList: ArrayList<Deal> = arrayListOf()
            val subscribe = openInsiderService.getInsiderTrading(insider)
                .map { doc ->
                    dealList = doAdditionalParsing(doc, "#subjectDetails")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    emitter.onSuccess(dealList)
                }, {
                    emitter.onError(it)
                })
            composite.add(subscribe)
        }
    }

    private fun doAdditionalParsing(document: Document, tagId: String) : ArrayList<Deal>
    {
        val listDeal : ArrayList<Deal> = arrayListOf()
        val elements = document.select("$tagId > table > tbody")
        if (elements.size > 0)
        {
            val tbody = elements[0]
            var trIndex = 1
            tbody.children().forEach {
                val filingDate = tbody.select("tr:nth-child($trIndex) > td:nth-child(2) > div > a").text()
                val deal = Deal(filingDate).apply {
                    filingDateRefer = tbody.select("tr:nth-child($trIndex) > td:nth-child(2) > div > a").attr("href")
                    tradeDate = tbody.select("tr:nth-child($trIndex) > td:nth-child(3) > div").text()
                    ticker = tbody.select("tr:nth-child($trIndex) > td:nth-child(4) > b > a").text()
                    company = ""
                    insiderName = tbody.select("tr:nth-child($trIndex) > td:nth-child(5) > a").text()
                    insiderNameRefer = tbody.select("tr:nth-child($trIndex) > td:nth-child(5) > a").attr("href")
                    insiderTitle = tbody.select("tr:nth-child($trIndex) > td:nth-child(6)").text()
                    tradeType = tbody.select("tr:nth-child($trIndex) > td:nth-child(7)").text()
                    price = tbody.select("tr:nth-child($trIndex) > td:nth-child(8)").text()
                    qty = tbody.select("tr:nth-child($trIndex) > td:nth-child(9)").text()
                    owned = tbody.select("tr:nth-child($trIndex) > td:nth-child(10)").text()
                    deltaOwn = tbody.select("tr:nth-child($trIndex) > td:nth-child(11)").text()
                    volumeStr = tbody.select("tr:nth-child($trIndex) > td:nth-child(12)").text()
                    trIndex++
                }
                listDeal.add(deal)
            }
        }
        return listDeal
    }

    fun getTradingByTicker(ticker: String): Single<ArrayList<Deal>>
    {
        return Single.create { emitter ->
            var dealList: ArrayList<Deal> = arrayListOf()
            val subscribe = openInsiderService.getTradingByTicker(ticker)
                .map { doc ->
                    dealList = doAdditionalParsing(doc, "#tablewrapper")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                               emitter.onSuccess(dealList)
                           }, {
                               emitter.onError(it)
                           })
            composite.add(subscribe)
        }
    }

}