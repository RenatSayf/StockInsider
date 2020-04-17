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


    //region Управление видимостью кнопки addAlarmImgView
    private val _addAlarmVisibility = MutableLiveData<Int>().apply {
        value = addAlarmVisibility?.value
    }
    val addAlarmVisibility: LiveData<Int> = _addAlarmVisibility
    fun setAddAlarmVisibility(visibility: Int)
    {
        _addAlarmVisibility.value = visibility
    }
    //endregion


    //region Управление видимостью кнопки alarmOnImgView
    private val _alarmOnVisibility = MutableLiveData<Int>().apply {
        value = alarmOnVisibility?.value
    }
    val alarmOnVisibility: LiveData<Int> = _alarmOnVisibility
    fun setAlarmOnVisibility(visibility: Int)
    {
        _alarmOnVisibility.value = visibility
    }
    //endregion


}
