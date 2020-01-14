package com.renatsayf.stockinsider.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.models.SearchRequest
import javax.inject.Inject

class HomeViewModel @Inject constructor() : ViewModel() {

    lateinit var searchRequest : SearchRequest

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

    suspend fun fetchTradingScreen(): String
    {
        val searchRequest = SearchRequest()
        val screen = searchRequest.fetchTradingScreen()
        return screen
    }
}