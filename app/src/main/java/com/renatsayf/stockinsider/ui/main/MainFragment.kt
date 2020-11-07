package com.renatsayf.stockinsider.ui.main

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.activity.addCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.ui.dialogs.SearchListDialog
import com.renatsayf.stockinsider.ui.dialogs.WebViewDialog
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.AppLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.date_layout.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.general_layout.*
import kotlinx.android.synthetic.main.group_layout.*
import kotlinx.android.synthetic.main.insider_layout.*
import kotlinx.android.synthetic.main.ticker_layout.view.*
import kotlinx.android.synthetic.main.traded_layout.*
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment()
{
    private lateinit var mainViewModel : MainViewModel
    private lateinit var searchDialogListObserver : SearchListDialog.EventObserver //TODO Sending events between Activities/Fragments: Step 6
    private lateinit var searchName : String

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
        searchName = getString(R.string.text_current_set_name)

        searchDialogListObserver = ViewModelProvider(requireActivity())[SearchListDialog.EventObserver::class.java]
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        val isAgree = requireActivity().getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE).getBoolean(MainActivity.KEY_IS_AGREE, false)
        if (!isAgree) WebViewDialog().show(requireActivity().supportFragmentManager, WebViewDialog.TAG)

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
                ticker_ET.setText("")
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

        CoroutineScope(Dispatchers.Main).launch {
            val roomSearchSet = mainViewModel.getSearchSetAsync(searchName)
            mainViewModel.setSearchSet(roomSearchSet)
        }

        //TODO Sending events between Activities/Fragments: Step 7 - observe data (complete)
        searchDialogListObserver.data.observe(viewLifecycleOwner, { event ->
            event.getContent()?.let {
                mainViewModel.setSearchSet(it)
            }
        })

        search_button.setOnClickListener {
            val set = RoomSearchSet(
                    searchName,
                    "",
                    ticker_ET.text.toString(),
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
            CoroutineScope(Dispatchers.Main).launch {
                val result = mainViewModel.saveSearchSet(set)
                if (result > -1)
                {
                    mainViewModel.setSearchSet(set)
                    val bundle = Bundle().apply {
                        putString(ResultFragment.ARG_QUERY_NAME, searchName)
                        putString(ResultFragment.ARG_TITLE, "Trading Screen")
                    }
                    search_button.findNavController().navigate(R.id.nav_result, bundle)
                }
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

        filingDateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long)
            {
                val array = requireContext().resources.getIntArray(R.array.value_for_filing_date)
                val selectedItem = array[p2]
                when
                {
                    selectedItem < 3 && selectedItem != 0 -> tradeDateSpinner.setSelection(3)
                    selectedItem == 0 -> tradeDateSpinner.setSelection(0)
                    else -> tradeDateSpinner.setSelection(p2)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?)
            {

            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this){
            (activity as MainActivity).finish()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId)
        {
            R.id.action_default_search ->
            {
                val searchName = getString(R.string.text_default_set_name)

                CoroutineScope(Dispatchers.Main).launch {
                    val roomSearchSet = mainViewModel.getSearchSetAsync(searchName)
                    mainViewModel.setSearchSet(roomSearchSet)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


}