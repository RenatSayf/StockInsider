package com.renatsayf.stockinsider.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DataTransferModel : ViewModel()
{
    private val _set = MutableLiveData<SearchSet>()

    fun setSearchSet(ticker : SearchSet)
    {
        _set.value = ticker
    }

    fun getSearchSet() : MutableLiveData<SearchSet>
    {
        return _set
    }

}