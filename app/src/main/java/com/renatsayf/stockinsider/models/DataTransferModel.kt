package com.renatsayf.stockinsider.models

import androidx.lifecycle.LiveData
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

    private val _dealList = MutableLiveData<ArrayList<Deal>>()
    fun setDealList(dealList : ArrayList<Deal>)
    {
        _dealList.value = dealList
    }
    fun getDealList() : MutableLiveData<ArrayList<Deal>>
    {
        return _dealList
    }

    private val _data = MutableLiveData<Any>()
    fun pushData(data: Any)
    {
        _data.value = data
    }
    var data: LiveData<Any> = _data

}