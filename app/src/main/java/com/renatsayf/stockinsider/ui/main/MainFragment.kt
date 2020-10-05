package com.renatsayf.stockinsider.ui.main

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
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.AppLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.date_layout.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.general_layout.*
import kotlinx.android.synthetic.main.group_layout.*
import kotlinx.android.synthetic.main.insider_layout.*
import kotlinx.android.synthetic.main.ticker_layout.view.*
import kotlinx.android.synthetic.main.traded_layout.*
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment()
{
    companion object
    {

    }
    private lateinit var mainViewModel : MainViewModel
    private lateinit var dataTransferModel : DataTransferModel
    private lateinit var defaultSet : String

    @Inject
    lateinit var confirmationDialog : ConfirmationDialog

    @Inject
    lateinit var appLog: AppLog

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

        when ((requireActivity() as MainActivity).isServiceRunning())
        {
            true -> alarmOffButton.visibility = View.VISIBLE
            else -> alarmOffButton.visibility = View.GONE
        }

        mainViewModel.companies.observe(viewLifecycleOwner, { companies ->
            val tickerListAdapter = companies?.let {
                TickersListAdapter(requireActivity(), it)
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
        defaultSet = getString(R.string.text_default_set_name)
        val roomSearchSet = mainViewModel.getSearchSet(defaultSet)
        mainViewModel.setSearchSet(roomSearchSet)

        search_button.setOnClickListener {
            val mainActivity = activity as MainActivity
            val searchSet = SearchSet("custom set")
            val tickersString = ticker_ET.text.toString().trim()
            searchSet.ticker = mainActivity.getTickersString(tickersString)
            searchSet.filingPeriod = mainActivity.getFilingOrTradeValue(filingDateSpinner.selectedItemPosition)
            searchSet.tradePeriod = mainActivity.getFilingOrTradeValue(tradeDateSpinner.selectedItemPosition)
            searchSet.isPurchase = mainActivity.getCheckBoxValue(purchaseCheckBox.isChecked)
            searchSet.isSale = mainActivity.getCheckBoxValue(saleCheckBox.isChecked)
            searchSet.tradedMin = traded_min_ET.text.toString().trim()
            searchSet.tradedMax = traded_max_ET.text.toString().trim()
            searchSet.isOfficer = mainActivity.getOfficerValue(officer_CheBox.isChecked)
            searchSet.isDirector = mainActivity.getCheckBoxValue(director_CheBox.isChecked)
            searchSet.isTenPercent = mainActivity.getCheckBoxValue(owner10_CheBox.isChecked)
            searchSet.groupBy = mainActivity.getGroupingValue(group_spinner.selectedItemPosition)
            searchSet.sortBy = mainActivity.getSortingValue(sort_spinner.selectedItemPosition)

            val set = RoomSearchSet(
                    defaultSet,
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
            (requireActivity() as MainActivity).hideKeyBoard(ticker_ET)
            val result = mainViewModel.saveSearchSet(set)
            if (result > -1)
            {
                val bundle = Bundle().apply {
                    putString(ResultFragment.ARG_SET_NAME, defaultSet)
                    putString(ResultFragment.ARG_TITLE, "Trading Screen")
                }
                search_button.findNavController().navigate(R.id.nav_result, bundle)
            }
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
                        requireActivity().let{ a ->
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

//        mainViewModel.dealList.apply {
//            value = null
//            observe(viewLifecycleOwner, { list ->
//                list?.let{
//                    requireActivity().loadProgreesBar?.visibility = View.GONE
//                    dataTransferModel.setDealList(list)
//                    search_button.findNavController().navigate(R.id.nav_result, null)
//                }
//            })
//        }
//
//        mainViewModel.documentError.apply {
//            value = null
//            observe(viewLifecycleOwner, {
//                it?.let {
//                    requireActivity().loadProgreesBar?.visibility = View.GONE
//                    when (it) {
//                        is IndexOutOfBoundsException ->
//                        {
//                            val dealList: ArrayList<Deal> = arrayListOf()
//                            dataTransferModel.setDealList(dealList)
//                            search_button.findNavController().navigate(R.id.nav_result, null)
//                        }
//                        else ->
//                        {
//                            Snackbar.make(search_button, it.message.toString(), Snackbar.LENGTH_LONG).show()
//                        }
//                    }
//                }
//            })
//        }
    }


}