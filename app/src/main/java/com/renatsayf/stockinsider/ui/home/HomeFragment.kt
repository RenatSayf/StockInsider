package com.renatsayf.stockinsider.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.db.RoomSearchSetDB
import com.renatsayf.stockinsider.ui.result.ResultFragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.group_layout.*
import kotlinx.android.synthetic.main.insider_layout.*
import kotlinx.android.synthetic.main.load_progress_layout.*
import kotlinx.android.synthetic.main.traded_layout.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

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
        return inflater.inflate(R.layout.fragment_home, container, false)
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


        search_button.setOnClickListener {
            val mainActivity = activity as MainActivity
            val request = homeViewModel.searchRequest
            request.tickers = ticker_ET.text.toString()
            request.filingDate = mainActivity.getFilingOrTradeValue(filingDateSpinner.selectedItemPosition)
            request.tradeDate = mainActivity.getFilingOrTradeValue(tradeDateSpinner.selectedItemPosition)
            request.groupBy = mainActivity.getGroupingValue(group_spinner.selectedItemPosition)
            request.sortBy = mainActivity.getSortingValue(sort_spinner.selectedItemPosition)
            request.isPurchase = mainActivity.getCheckBoxValue(purchaseCheckBox)
            request.isSale = mainActivity.getCheckBoxValue(saleCheckBox)
            request.isOfficer = mainActivity.getCheckBoxValue(officer_CheBox)
            request.isDirector = mainActivity.getCheckBoxValue(director_CheBox)
            request.isTenPercent = mainActivity.getCheckBoxValue(owner10_CheBox)
            request.tradedMin = traded_min_ET.text.toString()
            request.tradedMax = traded_max_ET.text.toString()
            mainActivity.loadProgreesBar.visibility = View.VISIBLE
            //homeViewModel.getHtmlDocument()
            request.fetchTradingScreen()

            val db = context?.let { it1 -> RoomSearchSetDB.getInstance(it1).searchSetDao() }
            GlobalScope.launch {
                val set = RoomSearchSet(
                        Date().time.toString(),
                        "custom set",
                        "",
                        ticker_ET.text.toString(),
                        filingDateSpinner.selectedItemPosition,
                        tradeDateSpinner.selectedItemPosition,
                        purchaseCheckBox.isChecked,
                        saleCheckBox.isChecked,
                        traded_min_ET.text.toString(),
                        traded_max_ET.text.toString(),
                        officer_CheBox.isChecked,
                        director_CheBox.isChecked,
                        owner10_CheBox.isChecked,
                        group_spinner.selectedItemPosition,
                        sort_spinner.selectedItemPosition
                                       )
                val res = db?.insertOrUpdateSearchSet(set)
                return@launch
            }
            return@setOnClickListener
        }

        homeViewModel.networkError.observe(viewLifecycleOwner, Observer {
            activity?.loadProgreesBar?.visibility = View.GONE
            if (!it.hasBeenHandled)
            {
                Snackbar.make(search_button, it.getContent()?.message.toString(), Snackbar.LENGTH_LONG).show()
            }
        })

        homeViewModel.networkSuccess.observe(viewLifecycleOwner, Observer {
            activity?.loadProgreesBar?.visibility = View.GONE
            val bundle = Bundle()
            if (!it.hasBeenHandled)
            {
                bundle.putParcelableArrayList(ResultFragment.TAG, it.getContent())
                activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.resultFragment, bundle)
            }
        })
    }

    override fun onDestroy()
    {
        disposable?.dispose()
        super.onDestroy()
    }


}