package com.renatsayf.stockinsider.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.db.RoomSearchSetDB
import com.renatsayf.stockinsider.models.DataTransferModel
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.ui.result.ResultFragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.group_layout.*
import kotlinx.android.synthetic.main.insider_layout.*
import kotlinx.android.synthetic.main.load_progress_layout.*
import kotlinx.android.synthetic.main.traded_layout.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeFragment : Fragment()
{

    private lateinit var homeViewModel : HomeViewModel
    private lateinit var dataTransferModel : DataTransferModel
    private var disposable : Disposable? = null
    private var db : RoomSearchSetDB? = null

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        db = activity?.applicationContext?.let { RoomSearchSetDB.getInstance(it) }
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

        val tickerArray = activity?.resources?.getStringArray(R.array.ticker_list)
        val tickerListAdapter = tickerArray?.let {
            ArrayAdapter<String>(
                    activity!!.applicationContext, R.layout.support_simple_spinner_dropdown_item, it
                                )
        }
        ticker_ET.setAdapter(tickerListAdapter)
        //ticker_ET.threshold = 1

        CoroutineScope(IO).launch {
            try
            {
                val customSet = async {
                    db?.searchSetDao()?.getSetByName("custom set")
                }.await()
                if (customSet != null)
                {
                    try
                    {
                        ticker_ET.setText(customSet.ticker)
                        filingDateSpinner.setSelection(customSet.filingPeriod)
                        tradeDateSpinner.setSelection(customSet.tradePeriod)
                        purchaseCheckBox.isChecked = customSet.isPurchase
                        saleCheckBox.isChecked = customSet.isSale
                        traded_min_ET.setText(customSet.tradedMin)
                        traded_max_ET.setText(customSet.tradedMax)
                        officer_CheBox.isChecked = customSet.isOfficer
                        director_CheBox.isChecked = customSet.isDirector
                        owner10_CheBox.isChecked = customSet.isTenPercent
                        group_spinner.setSelection(customSet.groupBy)
                        sort_spinner.setSelection(customSet.sortBy)
                    }
                    catch (e : Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }
            catch (e : Exception)
            {
                e.printStackTrace()
            }
        }

        search_button.setOnClickListener {
            homeViewModel.setTicker(ticker_ET.text.toString())
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
            val searchSet = SearchSet("custom set")
            searchSet.ticker = ticker_ET.text.toString()
            searchSet.filingPeriod = mainActivity.getFilingOrTradeValue(filingDateSpinner.selectedItemPosition)
            searchSet.tradePeriod = mainActivity.getFilingOrTradeValue(tradeDateSpinner.selectedItemPosition)
            searchSet.isPurchase = mainActivity.getCheckBoxValue(purchaseCheckBox)
            searchSet.isSale = mainActivity.getCheckBoxValue(saleCheckBox)
            searchSet.tradedMin = traded_min_ET.text.toString()
            searchSet.tradedMax = traded_max_ET.text.toString()
            searchSet.isOfficer = mainActivity.getCheckBoxValue(officer_CheBox)
            searchSet.isDirector = mainActivity.getCheckBoxValue(director_CheBox)
            searchSet.isTenPercent = mainActivity.getCheckBoxValue(owner10_CheBox)
            searchSet.groupBy = mainActivity.getGroupingValue(group_spinner.selectedItemPosition)
            searchSet.sortBy = mainActivity.getSortingValue(sort_spinner.selectedItemPosition)
            dataTransferModel.setSearchSet(searchSet)

            //homeViewModel.getHtmlDocument()
            request.fetchTradingScreen()

            CoroutineScope(IO).launch {
                val setName = filingDateSpinner.selectedItem.toString() +
                        tradeDateSpinner.selectedItem.toString() +
                        purchaseCheckBox.isChecked.toString() +
                        saleCheckBox.isChecked.toString() +
                        traded_min_ET.text.toString() +
                        traded_max_ET.text.toString() +
                        officer_CheBox.isChecked.toString() +
                        director_CheBox.isChecked.toString() +
                        owner10_CheBox.isChecked.toString() +
                        group_spinner.selectedItem.toString() +
                        sort_spinner.selectedItem.toString()
                val set = RoomSearchSet(
                        "",
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
                val res = async {
                    db?.searchSetDao()?.insertOrUpdateSearchSet(set)
                }.await()
            }
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