package com.renatsayf.stockinsider.ui.deal

import android.graphics.drawable.Drawable
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import com.renatsayf.stockinsider.utils.AppLog
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Single
import io.reactivex.functions.Consumer


class DealViewModel @ViewModelInject constructor(
        private val repositoryImpl: DataRepositoryImpl,
        private val appLog: AppLog
) : ViewModel()
{
    private val TAG = this::class.java.canonicalName.toString()

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
        appLog.print(TAG, "********************* onCleared() **************************")
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
