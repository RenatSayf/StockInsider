package com.renatsayf.stockinsider.ui.deal

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import javax.inject.Inject


@HiltViewModel
class DealViewModel @Inject constructor(
        private val repositoryImpl: DataRepositoryImpl
) : ViewModel()
{
    private var _background = MutableLiveData<Drawable>().apply {
        value = background?.value
    }
    var background: LiveData<Drawable> = _background

    fun setLayOutBackground(drawable: Drawable)
    {
        _background.value = drawable
    }

    fun getInsiderTrading(insider: String): Single<ArrayList<Deal>>
    {
        return repositoryImpl.getInsiderTradingFromNetAsync(insider)
    }

    override fun onCleared()
    {
        super.onCleared()
        repositoryImpl.destructor()
    }

    private var _ticker = MutableLiveData<String>().apply {
        value = ticker?.value
    }
    var ticker : LiveData<String> = _ticker
    fun setTicker(value: String)
    {
        _ticker.value = value
    }

    private var _deal = MutableLiveData<Deal>()
    var deal: LiveData<Deal> = _deal
    fun setDeal(value: Deal)
    {
        _deal.value = value
    }

    private var _chart = MutableLiveData<Drawable>()
    var chart: LiveData<Drawable> = _chart
    fun setChart(chart: Drawable)
    {
        _chart.value = chart
    }

    fun getTradingByTicker(ticker: String) : Single<ArrayList<Deal>>
    {
        return repositoryImpl.getTradingByTickerAsync(ticker)
    }
}
