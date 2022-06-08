package com.renatsayf.stockinsider.ui.strategy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class StrategyViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This section is under development"
    }
    val text: LiveData<String> = _text
}