package com.renatsayf.stockinsider.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.network.CallableAction
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.group_layout.*
import kotlinx.android.synthetic.main.insider_layout.*
import kotlinx.android.synthetic.main.traded_layout.*
import org.jsoup.nodes.Document

class HomeFragment : Fragment()
{

    private lateinit var homeViewModel : HomeViewModel
    private var disposable : Disposable? = null

    override fun onCreateView(
            inflater : LayoutInflater,
            container : ViewGroup?,
            savedInstanceState : Bundle?
                             ) : View?
    {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        val tickerArray = activity?.resources?.getStringArray(R.array.ticker_list)
        val tickerListAdapter = tickerArray?.let {
            ArrayAdapter<String>(
                    activity!!.applicationContext, R.layout.support_simple_spinner_dropdown_item, it
                                )
        }
        ticker_ET.setAdapter(tickerListAdapter)
        //ticker_ET.threshold = 1

        ticker_ET.doOnTextChanged { text, start, count, after ->

        }

        search_button.setOnClickListener {
            val mainActivity = activity as MainActivity
            homeViewModel.searchRequest.tickers = ticker_ET.text.toString()
            homeViewModel.searchRequest.filingDate = mainActivity.getFilingOrTradeValue(filingDateSpinner.selectedItemPosition)
            homeViewModel.searchRequest.tradeDate = mainActivity.getFilingOrTradeValue(tradeDateSpinner.selectedItemPosition)
            homeViewModel.searchRequest.groupBy = mainActivity.getGroupingValue(group_spinner.selectedItemPosition)
            homeViewModel.searchRequest.sortBy = mainActivity.getSortingValue(sort_spinner.selectedItemPosition)
            homeViewModel.searchRequest.isPurchase = mainActivity.getCheckBoxValue(purchaseCheckBox)
            homeViewModel.searchRequest.isSale = mainActivity.getCheckBoxValue(saleCheckBox)
            homeViewModel.searchRequest.isOfficer = mainActivity.getCheckBoxValue(officer_CheBox)
            homeViewModel.searchRequest.isDirector = mainActivity.getCheckBoxValue(director_CheBox)
            homeViewModel.searchRequest.isTenPercent = mainActivity.getCheckBoxValue(owner10_CheBox)
            homeViewModel.searchRequest.tradedMin = traded_min_ET.text.toString()
            homeViewModel.searchRequest.tradedMax = traded_max_ET.text.toString()

            //homeViewModel.searchRequest.fetchTradingScreen()

            val callable = Observable.fromCallable(CallableAction(homeViewModel.searchRequest))
            disposable = callable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ doc : Document? ->
                                                                         val body = doc?.body()
                    return@subscribe
                                                                     }, { err : Throwable? ->
                                                                         err?.printStackTrace()
                                                                     })

            return@setOnClickListener
        }
    }

    override fun onDestroy()
    {
        disposable?.dispose()
        super.onDestroy()
    }

}