package com.renatsayf.stockinsider.ui.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter

class ResultViewModel : ViewModel()
{
    private val _dealListAdapter = MutableLiveData<DealListAdapter>().apply {
        value = dealListAdapter?.value
    }
    val dealListAdapter : LiveData<DealListAdapter> = _dealListAdapter

}
