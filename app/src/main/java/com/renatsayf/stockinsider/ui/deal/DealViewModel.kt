package com.renatsayf.stockinsider.ui.deal

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


@HiltViewModel
class DealViewModel @Inject constructor(
        private val repositoryImpl: DataRepositoryImpl
) : ViewModel()
{
    private var composite = CompositeDisposable()

    fun getInsiderDeals(insider: String): LiveData<ArrayList<Deal>> {
        val deals = MutableLiveData<ArrayList<Deal>>()
        composite.add(repositoryImpl.getInsiderTradingFromNetAsync(insider)
                .subscribe({ list ->
                   deals.value = list
                }, { t ->
                    t.printStackTrace()
                    deals.value = arrayListOf()
                }))
        return deals
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

    fun getTradingByTicker(ticker: String) : LiveData<ArrayList<Deal>>
    {
        val deals = MutableLiveData<ArrayList<Deal>>()
        composite.add(repositoryImpl.getTradingByTickerAsync(ticker)
                .subscribe({ list ->
                    deals.value = list
                }, { t ->
                    t.printStackTrace()
                    deals.value = arrayListOf()
                }))
        return deals
    }

    override fun onCleared()
    {
        super.onCleared()
        composite.apply {
            dispose()
            clear()
        }
        repositoryImpl.destructor()
    }
}
