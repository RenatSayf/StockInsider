package com.renatsayf.stockinsider.network

import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.firebase.FireBaseViewModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject

class NetRepository @Inject constructor(private val api: IApi) : INetRepository
{
    var composite = CompositeDisposable()
    private var searchTicker : String = ""

    private val userAgent = try {
        FireBaseViewModel.userAgent
    }
    catch (e: ExceptionInInitializerError) {
        okhttp3.internal.userAgent
    }
    catch (e: NoClassDefFoundError) {
        okhttp3.internal.userAgent
    }
    catch (e: Exception) {
        okhttp3.internal.userAgent
    }

    override fun getTradingScreen(set: SearchSet) : io.reactivex.Observable<ArrayList<Deal>>
    {
        return io.reactivex.Observable.create {emitter ->
            var dealList: ArrayList<Deal> = arrayListOf()
            searchTicker = set.ticker
            val subscriber = api.getTradingScreen(
                set.ticker,
                set.filingPeriod,
                set.tradePeriod,
                set.isPurchase,
                set.isSale,
                set.excludeDerivRelated,
                set.tradedMin,
                set.tradedMax,
                set.isOfficer,
                set.isDirector,
                set.isTenPercent,
                set.groupBy,
                set.sortBy,
                agent = userAgent
            )
                .map { document ->
                    dealList = doMainParsing(document)
                }
                .subscribe({
                    if (!emitter.isDisposed) {
                        emitter.onNext(dealList)
                    }
                }, { error: Throwable ->

                    if (!emitter.isDisposed) {
                        emitter.onError(error)
                    }
                }, {
                    if (!emitter.isDisposed) {
                        emitter.onComplete()
                    }
                })
            composite.add(subscriber)
        }
    }

    fun doMainParsing(document : Document) : ArrayList<Deal>
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

    suspend fun doMainParsingAsync(document: Document): Deferred<List<Deal>> {
        return coroutineScope {
            async {
                doMainParsing(document)
            }
        }
    }

    override fun getInsiderTrading(insider: String): Single<ArrayList<Deal>>
    {
        return Single.create { emitter ->
            var dealList: ArrayList<Deal> = arrayListOf()
            val subscribe = api.getInsiderTrading(insider, userAgent)
                .map { doc ->
                    dealList = doAdditionalParsing(doc, "#subjectDetails")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!emitter.isDisposed) {
                        emitter.onSuccess(dealList)
                    }
                }, {
                    if (!emitter.isDisposed) {
                        emitter.onError(it)
                    }
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
            val tBody = elements[0]
            var trIndex = 1
            tBody.children().forEach { element ->
                val filingDate = element.select("tr:nth-child($trIndex) > td:nth-child(2) > div > a").text()
                val deal = Deal(filingDate).apply {
                    filingDateRefer = element.select("tr:nth-child($trIndex) > td:nth-child(2) > div > a").attr("href")
                    tradeDate = element.select("tr:nth-child($trIndex) > td:nth-child(3) > div").text()
                    ticker = element.select("tr:nth-child($trIndex) > td:nth-child(4) > b > a").text()
                    company = ""
                    insiderName = element.select("tr:nth-child($trIndex) > td:nth-child(5) > a").text()
                    insiderNameRefer = element.select("tr:nth-child($trIndex) > td:nth-child(5) > a").attr("href")
                    insiderTitle = element.select("tr:nth-child($trIndex) > td:nth-child(6)").text()
                    tradeType = element.select("tr:nth-child($trIndex) > td:nth-child(7)").text()
                    price = element.select("tr:nth-child($trIndex) > td:nth-child(8)").text()
                    qty = element.select("tr:nth-child($trIndex) > td:nth-child(9)").text()
                    owned = element.select("tr:nth-child($trIndex) > td:nth-child(10)").text()
                    deltaOwn = element.select("tr:nth-child($trIndex) > td:nth-child(11)").text()
                    volumeStr = element.select("tr:nth-child($trIndex) > td:nth-child(12)").text()
                    trIndex++
                }
                listDeal.add(deal)
            }
        }
        return listDeal
    }

    override fun getTradingByTicker(ticker: String): Single<ArrayList<Deal>> {

        return Single.create { emitter ->
            var dealList: ArrayList<Deal> = arrayListOf()
            val subscribe = api.getTradingByTicker(ticker, userAgent)
                .map { doc ->
                    dealList = doAdditionalParsing(doc, "#tablewrapper")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!emitter.isDisposed) {
                        emitter.onSuccess(dealList)
                    }
                }, {
                    if (!emitter.isDisposed) {
                        emitter.onError(it)
                    }
                })
            composite.add(subscribe)
        }
    }

    override fun getAllCompaniesName(): Single<List<Company>> {

        val set = RoomSearchSet(
            queryName = "",
            companyName = "",
            ticker = "",
            filingPeriod = 11,
            tradePeriod = 11,
            isPurchase = true,
            isSale = true,
            tradedMin = "",
            tradedMax = "",
            isOfficer = true,
            isDirector = true,
            isTenPercent = true,
            groupBy = 0,
            sortBy = 3
        ).toSearchSet()

        return Single.create { emitter ->
            val subscribe = api.getTradingScreen(
                set.ticker,
                set.filingPeriod,
                set.tradePeriod,
                set.isPurchase,
                set.isSale,
                set.excludeDerivRelated,
                set.tradedMin,
                set.tradedMax,
                set.isOfficer,
                set.isDirector,
                set.isTenPercent,
                set.groupBy,
                set.sortBy,
                agent = userAgent
            )
                .map { document ->
                doAllCompanyNameParsing(document)
            }
                .subscribe({ set ->
                    if (!emitter.isDisposed) {
                        emitter.onSuccess(set.toList())
                    }
                }, { t ->
                    if (!emitter.isDisposed) {
                        emitter.onError(t)
                    }
                })
            composite.add(subscribe)
        }
    }

    override suspend fun getDealsListAsync(set: SearchSet): Deferred<List<Deal>> {
        return coroutineScope {
            async {
                val response = api.getDealsListAsync(
                    set.ticker,
                    set.filingPeriod,
                    set.tradePeriod,
                    set.isPurchase,
                    set.isSale,
                    set.excludeDerivRelated,
                    set.tradedMin,
                    set.tradedMax,
                    set.isOfficer,
                    set.isDirector,
                    set.isTenPercent,
                    set.groupBy,
                    set.sortBy,
                    agent = userAgent
                )
                val document = response.body()
                if (document != null) doMainParsingAsync(document).await()
                else listOf()
            }
        }
    }

    fun doAllCompanyNameParsing(document: Document?): Set<Company> {

        val companySet : MutableSet<Company> = mutableSetOf()
        val body = document?.body()
        val tableBody = body?.select("#tablewrapper > table > tbody")
        val size = tableBody?.size ?: 0
        var trIndex = 1
        return if (size > 0) {
            val table = tableBody?.get(0)
            table?.children()?.forEach { element ->
                val ticker = element?.select("tr:nth-child($trIndex) > td:nth-child(4) > b > a")?.text()
                val tdIndex =  0
                val company = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 5}) > a")?.text()
                if (ticker != null && company != null) {
                    companySet.add(Company(ticker, company))
                }
                trIndex++
            }
            return companySet
        }
        else setOf()
    }



}