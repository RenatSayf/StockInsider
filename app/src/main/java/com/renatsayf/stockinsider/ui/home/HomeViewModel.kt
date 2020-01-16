package com.renatsayf.stockinsider.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.models.SearchRequest
import com.renatsayf.stockinsider.network.GetHtmlDocAction
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject

class HomeViewModel : ViewModel() {

    @Inject
    lateinit var searchRequest : SearchRequest
    private var disposable : Disposable? = null
    private var body : Element? = null

    init
    {
        App().component.inject(this)
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
                           body = doc?.body()
                           val tableBody = body?.select("#tablewrapper > table > tbody")
                return@subscribe
                       }, { err : Throwable? ->
                           err?.printStackTrace()
                       })
    }

}