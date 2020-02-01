package com.renatsayf.stockinsider.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.RoomDBProvider
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.db.SearchSetDao
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.models.DataTransferModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.group_layout.*
import kotlinx.android.synthetic.main.insider_layout.*
import kotlinx.android.synthetic.main.load_progress_layout.*
import kotlinx.android.synthetic.main.traded_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

class HomeFragment : Fragment(), SearchRequest.Companion.IDocumentListener
{

    private lateinit var homeViewModel : HomeViewModel
    private lateinit var dataTransferModel : DataTransferModel
    private var db : SearchSetDao? = null

    @Inject
    lateinit var searchRequest : SearchRequest

    @Inject
    lateinit var dbProvider : RoomDBProvider

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        App().component.inject(this)
        searchRequest.setOnDocumentReadyListener(this)
        db = context?.let { dbProvider.getDataBase(context as MainActivity) }
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
                    db?.getSetByName("custom set")
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
            val mainActivity = activity as MainActivity
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
            searchRequest.getTradingScreen(searchSet)

            CoroutineScope(IO).launch {
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
                withContext(Dispatchers.Default) {
                    db?.insertOrUpdateSearchSet(set)
                }
            }
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