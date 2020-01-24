package com.renatsayf.stockinsider.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.network.GetHtmlDocAction
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.utils.Event
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject

class HomeViewModel : ViewModel(), SearchRequest.Companion.IDocumentListener
{
    val networkError = MutableLiveData<Event<Throwable>>()
    val networkSuccess = MutableLiveData<Event<ArrayList<Deal>>>()

    @Inject
    lateinit var searchRequest : SearchRequest
    private var disposable : Disposable? = null

    init
    {
        App().component.inject(this)
        searchRequest.setOnDocumentReadyListener(this)
    }

    private val _ticker = MutableLiveData<String>().apply {
        value = ticker?.value
    }
    var ticker: LiveData<String> = _ticker

    fun setTicker(value: String)
    {
        _ticker.value = value
        searchRequest.tickers = _ticker.value.toString()
    }

    fun getHtmlDocument()
    {
        val callable = Observable.fromCallable(GetHtmlDocAction(searchRequest))
        disposable = callable.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ doc : Document? ->
                           doc?.let { onDocumentReady(it) }
                return@subscribe
                       }, { err : Throwable ->
                           onDocumentError(err)
                       })
    }

    override fun onDocumentReady(document : Document)
    {
        val listDeal : ArrayList<Deal> = arrayListOf()
        val body = document.body()
        val tableBody = body?.select("#tablewrapper > table > tbody")
        val table = tableBody?.get(0)
        var i = 1
        table?.children()?.forEach { element: Element? ->
            val filingDate = element?.select("tr:nth-child($i) > td:nth-child(2) > div > a")?.text()
            val deal = Deal(filingDate)
            deal.filingDateRefer = element?.select("tr:nth-child($i) > td:nth-child(2) > div > a")?.attr("href")
            deal.tradeDate = element?.select("tr:nth-child($i) > td:nth-child(3) > div")?.text()
            deal.ticker = element?.select("tr:nth-child($i) > td:nth-child(4) > b > a")?.text()
            deal.company = element?.select("tr:nth-child($i) > td:nth-child(5) > a")?.text()
            deal.insiderName = element?.select("tr:nth-child($i) > td:nth-child(6) > a")?.text()
            deal.insiderNameRefer = element?.select("tr:nth-child($i) > td:nth-child(6) > a")?.attr("href")
            deal.insiderTitle = element?.select("tr:nth-child($i) > td:nth-child(7)")?.text()
            deal.tradeType = element?.select("tr:nth-child($i) > td:nth-child(8)")?.text()
            deal.price = element?.select("tr:nth-child($i) > td:nth-child(9)")?.text()
            deal.volumeStr = element?.select("tr:nth-child($i) > td:nth-child(13)")?.text().toString()
            listDeal.add(deal)
            i++
        }

        listDeal.let { _ ->
            networkSuccess.value = Event(listDeal)
        }

    }

    override fun onDocumentError(throwable : Throwable)
    {
        networkError.value = Event(throwable)
    }

}