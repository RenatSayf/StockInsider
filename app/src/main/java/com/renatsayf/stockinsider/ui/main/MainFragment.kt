package com.renatsayf.stockinsider.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.DataTransferModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.date_layout.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.general_layout.*
import kotlinx.android.synthetic.main.group_layout.*
import kotlinx.android.synthetic.main.insider_layout.*
import kotlinx.android.synthetic.main.load_progress_layout.*
import kotlinx.android.synthetic.main.ticker_layout.view.*
import kotlinx.android.synthetic.main.traded_layout.*
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment()
{
    companion object
    {
        const val DEFAULT_SET : String = "default set"
    }
    private lateinit var mainViewModel : MainViewModel
    private lateinit var dataTransferModel : DataTransferModel
    private var isScreenRotate: Boolean = false

    @Inject
    lateinit var searchRequest : SearchRequest

    @Inject
    lateinit var confirmationDialog : ConfirmationDialog

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

        activity?.let { a ->
            when((a as MainActivity).isServiceRunning())
            {
                true -> alarmOffButton.visibility = View.VISIBLE
                else -> alarmOffButton.visibility = View.GONE
            }
        }

        mainViewModel.companies.observe(viewLifecycleOwner, { companies ->
            val tickerListAdapter = companies?.let {
                context?.let { _ ->
                    TickersListAdapter(activity as MainActivity, it)
                }
            }
            ticker_ET.setAdapter(tickerListAdapter)
            //ticker_ET.threshold = 1
        })

        var tickerText = ""
        ticker_ET.doOnTextChanged { text, _, _, _ ->
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

        ticker_ET.setOnItemClickListener { _, view, _, _ ->
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

        mainViewModel.searchSet.observe(viewLifecycleOwner, {
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
        val roomSearchSet = mainViewModel.getSearchSet(DEFAULT_SET)
        mainViewModel.setSearchSet(roomSearchSet)

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

        alarmOffButton.setOnClickListener {
            confirmationDialog.message = getString(R.string.text_cancel_search)
            confirmationDialog.flag = ConfirmationDialog.FLAG_CANCEL
            confirmationDialog.show(parentFragmentManager, ConfirmationDialog.TAG)
        }

        confirmationDialog.eventOk.observe(viewLifecycleOwner, {
            if (!it.hasBeenHandled)
            {
                it.getContent().let { flag ->
                    if (flag == ConfirmationDialog.FLAG_CANCEL)
                    {
                        activity?.let{ a ->
                            (a as MainActivity).setAlarmSetting(false)
                            AlarmPendingIntent.cancel(a)
                        }
                    }
                }
            }
        })

        StockInsiderService.serviceEvent.observe(viewLifecycleOwner, {
            if (!it.hasBeenHandled)
            {
                if (it.getContent() == StockInsiderService.STOP_KEY)
                {
                    context?.getString(R.string.text_search_is_disabled)?.let { msg ->
                        alarmOffButton.visibility = View.GONE
                        Snackbar.make(search_button, msg, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun onDocumentError(throwable : Throwable)
    {
        activity?.loadProgreesBar?.visibility = View.GONE
        when (throwable) {
            is IndexOutOfBoundsException ->
            {
                val dealList: ArrayList<Deal> = arrayListOf()
                dataTransferModel.setDealList(dealList)
                search_button.findNavController().navigate(R.id.nav_result, null)
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
        search_button.findNavController().navigate(R.id.nav_result, null)

    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        isScreenRotate = true
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?)
    {
        super.onViewStateRestored(savedInstanceState)
        isScreenRotate = false
    }

    override fun onDestroy()
    {
        super.onDestroy()
        (activity as MainActivity).let { a ->
            val isServiceEnabled = a.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE).getBoolean(AlarmPendingIntent.IS_ALARM_SETUP_KEY, false)
            if (isServiceEnabled && !isScreenRotate)
            {
                val serviceIntent = Intent(a, StockInsiderService::class.java)
                a.startService(serviceIntent)
            }
        }
    }


}