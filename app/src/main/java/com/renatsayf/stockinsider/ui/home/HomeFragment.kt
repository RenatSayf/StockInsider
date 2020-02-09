package com.renatsayf.stockinsider.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doBeforeTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomDBProvider
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.models.DataTransferModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.group_layout.*
import kotlinx.android.synthetic.main.insider_layout.*
import kotlinx.android.synthetic.main.load_progress_layout.*
import kotlinx.android.synthetic.main.traded_layout.*
import javax.inject.Inject

class HomeFragment : Fragment(), SearchRequest.Companion.IDocumentListener
{
    companion object
    {
        const val DEFAULT_SET : String = "default set"
    }
    private lateinit var homeViewModel : HomeViewModel
    private lateinit var dataTransferModel : DataTransferModel
    private lateinit var db : AppDao

    @Inject
    lateinit var searchRequest : SearchRequest

    @Inject
    lateinit var dbProvider : RoomDBProvider

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        App().component.inject(this)
        searchRequest.setOnDocumentReadyListener(this)
        db = dbProvider.getDao(context as MainActivity)
    }

    override fun onCreateView(
            inflater : LayoutInflater,
            container : ViewGroup?,
            savedInstanceState : Bundle?
                             ) : View?
    {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        dataTransferModel = activity?.run {
            ViewModelProvider(activity as MainActivity)[DataTransferModel::class.java]
        }!!
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        homeViewModel.tickers.observe(viewLifecycleOwner, Observer { tickers ->
            if (tickers == null)
            {
                homeViewModel.getTickersArray(db)
            }

            val tickerListAdapter = tickers?.let {
                context?.let { context ->
                    TickersListAdapter(activity as MainActivity, android.R.layout.simple_list_item_1, it)
                }
            }
            ticker_ET.setAdapter(tickerListAdapter)
            //ticker_ET.threshold = 1
        })

        var tickerText = ""
        ticker_ET.doBeforeTextChanged { text, start, count, after ->
            tickerText = text.toString()
        }

        ticker_ET.setOnItemClickListener { adapterView, view, i, l ->
            val item = adapterView.adapter.getItem(i).toString()
            ticker_ET.setText(tickerText.plus(item))
        }


        homeViewModel.searchSet.observe(viewLifecycleOwner, Observer {
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
        homeViewModel.getSearchSetByName(db, DEFAULT_SET)

        search_button.setOnClickListener {
            val mainActivity = activity as MainActivity
            mainActivity.loadProgreesBar.visibility = View.VISIBLE
            val searchSet = SearchSet("custom set")
            searchSet.ticker = ticker_ET.text.toString().trim()
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
            homeViewModel.saveDefaultSearch(db, set)
        }
    }

    override fun onDocumentError(throwable : Throwable)
    {
        activity?.loadProgreesBar?.visibility = View.GONE
        Snackbar.make(search_button, throwable.message.toString(), Snackbar.LENGTH_LONG).show()
    }

    override fun onDealListReady(dealList : ArrayList<Deal>)
    {
        activity?.loadProgreesBar?.visibility = View.GONE
        dataTransferModel.setDealList(dealList)
        activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.resultFragment, null)
    }


}