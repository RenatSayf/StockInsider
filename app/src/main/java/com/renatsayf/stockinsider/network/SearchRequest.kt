package com.renatsayf.stockinsider.network

import android.content.Context
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject

class SearchRequest @Inject constructor(private val context: Context)
{
    private var openInsiderService: OpenInsiderService = OpenInsiderService.create()
    private var disposable : Disposable? = null
    private var searchTicker : String = ""

    fun getTradingScreen(set: SearchSet) : io.reactivex.Observable<ArrayList<Deal>>
    {
        return io.reactivex.Observable.create {emitter ->
            var dealList: ArrayList<Deal> = arrayListOf()
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
                .map { document ->
                    dealList = doParseDocument(document)
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
        }
    }

    private fun doParseDocument(document : Document) : ArrayList<Deal>
    {
        val listDeal : ArrayList<Deal> = arrayListOf()
        val body = document.body()
        val bodyText = body.text()
        if (bodyText != null && bodyText.contains("ERROR:"))
        {
            val deal = Deal("")
            deal.error = context.getString(R.string.text_data_not_avalible)
            listDeal.add(deal)
            return listDeal
        }
        val error = body?.select("#tablewrapper")?.text()
        if (error != null && error.contains("ERROR:"))
        {
            val deal = Deal("")
            deal.error = context.getString(R.string.text_data_not_avalible)
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





}