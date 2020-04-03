package com.renatsayf.stockinsider.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hypertrack.hyperlog.HyperLog
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.DataTransferModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.group_layout.*
import kotlinx.android.synthetic.main.insider_layout.*
import kotlinx.android.synthetic.main.load_progress_layout.*
import kotlinx.android.synthetic.main.ticker_layout.view.*
import kotlinx.android.synthetic.main.traded_layout.*
import java.lang.IndexOutOfBoundsException
import javax.inject.Inject

class MainFragment @Inject constructor() : Fragment()
{
    companion object
    {
        const val DEFAULT_SET : String = "default set"
    }
    private lateinit var mainViewModel : MainViewModel
    private lateinit var dataTransferModel : DataTransferModel

    @Inject
    lateinit var searchRequest : SearchRequest

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        MainActivity.appComponent.inject(this)
    }

    override fun onCreateView(
            inflater : LayoutInflater,
            container : ViewGroup?,
            savedInstanceState : Bundle?
                             ) : View?
    {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        dataTransferModel = activity?.run {
            ViewModelProvider(activity as MainActivity)[DataTransferModel::class.java]
        }!!
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        mainViewModel.companies.observe(viewLifecycleOwner, Observer { companies ->
            if (companies == null)
            {
                mainViewModel.getCompaniesArray()
            }
            val tickerListAdapter = companies?.let {
                context?.let { _ ->
                    TickersListAdapter(activity as MainActivity, it)
                }
            }
            ticker_ET.setAdapter(tickerListAdapter)
            //ticker_ET.threshold = 1
        })

        var tickerText = ""
        ticker_ET.doOnTextChanged { text, start, count, after ->
            if (text != null)
            {
                if (text.isNotEmpty())
                {
                    val c = text[text.length - 1]
                    if (c.isWhitespace())
                    {
                        tickerText = text.toString()
                    }
                }
                else
                {
                    tickerText = ""
                }
            }
        }

        ticker_ET.setOnItemClickListener { _, view, i, l ->
            val tickerLayout = view.tickerLayout
            val ticker = tickerLayout.tickerTV.text
            val str = (tickerText.plus(ticker)).trim()
            ticker_ET.setText(str)
            ticker_ET.setSelection(str.length)
        }

        clearTicketImView.setOnClickListener {
            ticker_ET.setText("")
            tickerText = ""
        }

        mainViewModel.searchSet.observe(viewLifecycleOwner, Observer {
            if (it != null)
            {
                ticker_ET.setText(it.ticker)
                filingDateSpinner.setSelection(it.filingPeriod)
                tradeDateSpinner.setSelection(it.tradePeriod)
                purchaseCheckBox.isChecked = it.isPurchase
                saleCheckBox.isChecked = it.isSale
                traded_min_ET.setText(it.tradedMin)
                traded_max_ET.setText(it.tradedMax)
                officer_CheBox.isChecked = it.isOfficer
                director_CheBox.isChecked = it.isDirector
                owner10_CheBox.isChecked = it.isTenPercent
                group_spinner.setSelection(it.groupBy)
                sort_spinner.setSelection(it.sortBy)
            }
        })
        mainViewModel.getSearchSetByName(DEFAULT_SET)

        search_button.setOnClickListener {
            val mainActivity = activity as MainActivity
            mainActivity.loadProgreesBar.visibility = View.VISIBLE
            val searchSet = SearchSet("custom set")
            val tickersString = ticker_ET.text.toString().trim()
            searchSet.ticker = mainActivity.getTickersString(tickersString)
            searchSet.filingPeriod = mainActivity.getFilingOrTradeValue(filingDateSpinner.selectedItemPosition)
            searchSet.tradePeriod = mainActivity.getFilingOrTradeValue(tradeDateSpinner.selectedItemPosition)
            searchSet.isPurchase = mainActivity.getCheckBoxValue(purchaseCheckBox)
            searchSet.isSale = mainActivity.getCheckBoxValue(saleCheckBox)
            searchSet.tradedMin = traded_min_ET.text.toString().trim()
            searchSet.tradedMax = traded_max_ET.text.toString().trim()
            searchSet.isOfficer = mainActivity.getCheckBoxValue(officer_CheBox)
            searchSet.isDirector = mainActivity.getCheckBoxValue(director_CheBox)
            searchSet.isTenPercent = mainActivity.getCheckBoxValue(owner10_CheBox)
            searchSet.groupBy = mainActivity.getGroupingValue(group_spinner.selectedItemPosition)
            searchSet.sortBy = mainActivity.getSortingValue(sort_spinner.selectedItemPosition)

            searchRequest.getTradingScreen(searchSet)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({list ->
                    onDealListReady(list)
                }, {
                    t -> onDocumentError(t)
                })

            val set = RoomSearchSet(
                    DEFAULT_SET,
                    "",
                    ticker_ET.text.toString().trim(),
                    filingDateSpinner.selectedItemPosition,
                    tradeDateSpinner.selectedItemPosition,
                    purchaseCheckBox.isChecked,
                    saleCheckBox.isChecked,
                    traded_min_ET.text.toString().trim(),
                    traded_max_ET.text.toString().trim(),
                    officer_CheBox.isChecked,
                    director_CheBox.isChecked,
                    owner10_CheBox.isChecked,
                    group_spinner.selectedItemPosition,
                    sort_spinner.selectedItemPosition
                                   )
            mainActivity.hideKeyBoard(ticker_ET)
            mainViewModel.saveDefaultSearch(set)
        }
    }

    private fun onDocumentError(throwable : Throwable)
    {
        activity?.loadProgreesBar?.visibility = View.GONE
        when (throwable) {
            is IndexOutOfBoundsException ->
            {
                val dealList: ArrayList<Deal> = arrayListOf()
                dataTransferModel.setDealList(dealList)
                activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.resultFragment, null)
            }
            else ->
            {
                Snackbar.make(search_button, throwable.message.toString(), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun onDealListReady(dealList : ArrayList<Deal>)
    {
        activity?.loadProgreesBar?.visibility = View.GONE
        dataTransferModel.setDealList(dealList)
        activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.resultFragment, null)
    }



}