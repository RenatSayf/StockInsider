package com.renatsayf.stockinsider.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _ticker = MutableLiveData<String>().apply {
        value = ticker?.value
    }
    var ticker: LiveData<String> = _ticker

    fun setTicker(value: String)
    {
        _ticker.value = value
    }
}