package com.renatsayf.stockinsider.network

import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject

class NetRepository @Inject constructor(private val api: IApi) : INetRepository
{
    private var searchTicker : String = ""

    private val userAgent = try {
        FireBaseConfig.userAgent
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
    override suspend fun getTradingListAsync(set: SearchSet): Deferred<Result<List<Deal>>> {
        return coroutineScope {
            async {
                try {
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
                    if (response.isSuccessful) {
                        val document = response.body()
                        document?.let {
                            val list = doMainParsing(document).toList()
                            Result.success(list)
                        }?: run {
                            Result.success(emptyList())
                        }
                    }
                    else {
                        Result.failure(Throwable(response.message()))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }
    }

    fun doMainParsing(document : Document) : ArrayList<Deal>
    {
        val listDeal : ArrayList<Deal> = arrayListOf()
        val body = document.body()
        val bodyText = body.text()
        if (bodyText.contains("ERROR:"))
        {
            val deal = Deal("")
            deal.error = "ERROR:"
            listDeal.add(deal)
            return listDeal
        }
        val error = body.select("#tablewrapper").text()
        if (error.contains("ERROR:"))
        {
            val deal = Deal("")
            deal.error = "ERROR:"
            listDeal.add(deal)
            return listDeal
        }
        val tableBody = body.select("#tablewrapper > table > tbody")
        tableBody.let {
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
                    val tdIndex = when {
                        searchTicker.isNotEmpty() && !searchTicker.contains("+") -> {
                            -1
                        }
                        else -> 0
                    }
                    var company = element?.select("tr:nth-child($trIndex) > td:nth-child(${tdIndex + 5}) > a")?.text()
                    if (company.isNullOrEmpty()) {
                        company = document.select("#tablewrapper > div.h1title > h1").text()
                        company = company?.substringAfter(" - ", "")?.substringBefore(" - ", "")
                    }
                    deal.company = company
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

    private suspend fun doMainParsingAsync(document: Document): Deferred<List<Deal>> {
        return coroutineScope {
            async {
                doMainParsing(document)
            }
        }
    }
    override suspend fun getInsiderTradingAsync(insider: String): Deferred<Result<List<Deal>>> {
        return coroutineScope {
            async {
                try {
                    val response = api.getInsiderTradingAsync(insider, userAgent)
                    if (response.isSuccessful) {
                        response.body()?.let { doc ->
                            val deals = doAdditionalParsing(doc, "#subjectDetails")
                            Result.success(deals.toList())
                        }?: run {
                            Result.success(emptyList())
                        }
                    }
                    else {
                        Result.failure(Throwable(response.message()))
                    }
                }
                catch (e: Throwable) {
                    Result.failure(e)
                }
            }
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
                    company = (if (tagId == "#tablewrapper") document.select("$tagId > div.h1title > h1").text() else "")
                        .substringAfter(" - ", "").substringBefore(" - ", "")
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
    override suspend fun getDealsByTicker(ticker: String): Deferred<Result<List<Deal>>> {
        return coroutineScope {
            async {
                val response = api.getDealsByTicker(ticker, userAgent)
                if (response.isSuccessful) {
                    response.body()?.let { doc ->
                        val list = doAdditionalParsing(doc, "#tablewrapper").toList()
                        Result.success(list)
                    }?: run {
                        Result.success(emptyList())
                    }
                }
                else {
                    Result.failure(Throwable(response.message()))
                }
            }
        }
    }

    override suspend fun getAllCompaniesNameAsync(): Deferred<Result<List<Company>>> {
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
        return coroutineScope {
            async {
                try {
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
                    if (response.isSuccessful) {
                        response.body()?.let { doc ->
                            val companySet = doAllCompanyNameParsing(doc)
                            Result.success(companySet.toList())
                        }?: run {
                            Result.success(emptyList())
                        }
                    }
                    else {
                        Result.failure(Exception(response.message()))
                    }
                }
                catch (e: Exception) {
                    Result.failure(e)
                }
            }
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