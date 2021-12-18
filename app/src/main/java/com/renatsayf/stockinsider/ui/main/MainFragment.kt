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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentHomeBinding
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.ui.dialogs.SearchListDialog
import com.renatsayf.stockinsider.ui.dialogs.WebViewDialog
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLConnection

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_home)
{
    private lateinit var binding: FragmentHomeBinding
    private val mainVM : MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java].apply {
            getCurrentSearchSet(getString(R.string.text_current_set_name)).observe(this@MainFragment, {
                this.setState(MainViewModel.State.Initial(it))
            })
        }
    }
    private lateinit var searchName : String

    private lateinit var ad: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        ad = InterstitialAd(requireContext())

        if (savedInstanceState == null) {
            ad.apply {
                adUnitId = if (BuildConfig.DEBUG)
                {
                    requireContext().getString(R.string.test_interstitial_ads_id)
                }
                else
                {
                    requireContext().getString(R.string.interstitial_ad_2)
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    override fun onCreateView(
            inflater : LayoutInflater,
            container : ViewGroup?,
            savedInstanceState : Bundle?
                             ) : View?
    {
        searchName = getString(R.string.text_current_set_name)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        binding = FragmentHomeBinding.bind(view)

        val isAgree = requireActivity().getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE).getBoolean(MainActivity.KEY_IS_AGREE, false)
        if (!isAgree) WebViewDialog().show(requireActivity().supportFragmentManager, WebViewDialog.TAG)

        when ((requireActivity() as MainActivity).isServiceRunning())
        {
            true -> binding.alarmOffButton.visibility = View.VISIBLE
            else -> binding.alarmOffButton.visibility = View.GONE
        }

        mainVM.state.observe(viewLifecycleOwner, { state ->
            when(state) {
                is MainViewModel.State.Initial   ->
                {
                    val set = state.set
                    with(binding) {
                        general.tickerET.setText("")
                        general.tickerET.setText(set.ticker)
                        date.filingDateSpinner.setSelection(set.filingPeriod)
                        date.tradeDateSpinner.setSelection(set.tradePeriod)
                        traded.purchaseCheckBox.isChecked = set.isPurchase
                        traded.saleCheckBox.isChecked = set.isSale
                        traded.tradedMinET.setText(set.tradedMin)
                        traded.tradedMaxET.setText(set.tradedMax)
                        insider.officerCheBox.isChecked = set.isOfficer
                        insider.directorCheBox.isChecked = set.isDirector
                        insider.owner10CheBox.isChecked = set.isTenPercent
                        sorting.groupSpinner.setSelection(set.groupBy)
                        sorting.sortSpinner.setSelection(set.sortBy)
                    }
                    mainVM.saveSearchSet(set)
                    (requireActivity() as MainActivity).hideKeyBoard(binding.general.tickerET)
                    binding.general.tickerET.clearFocus()
                    binding.searchButton.requestFocus()
                }
            }
        })

        mainVM.companies.observe(viewLifecycleOwner, { companies ->
            val tickerListAdapter = companies?.let {
                TickersListAdapter(requireActivity(), it)
            }
            binding.general.tickerET.setAdapter(tickerListAdapter)
            binding.general.tickerET.clearFocus()
            (activity as MainActivity).hideKeyBoard(binding.general.tickerET)
        })

        var tickerText = ""
        binding.general.tickerET.doOnTextChanged { text, _, _, _ ->
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

        binding.general.tickerET.setOnItemClickListener { _, v, _, _ ->
            val tickerLayout = TickerLayoutBinding.bind(v)
            val ticker = tickerLayout.tickerTV.text
            val str = (tickerText.plus(ticker)).trim()
            binding.general.tickerET.setText(str)
            binding.general.tickerET.setSelection(str.length)
        }

        binding.general.clearTicketImView.setOnClickListener {
            binding.general.tickerET.setText("")
            tickerText = ""
        }

        binding.searchButton.setOnClickListener {
            with(binding) {
                val set = RoomSearchSet(
                    searchName,
                    "",
                    general.tickerET.text.toString(),
                    date.filingDateSpinner.selectedItemPosition,
                    date.tradeDateSpinner.selectedItemPosition,
                    traded.purchaseCheckBox.isChecked,
                    traded.saleCheckBox.isChecked,
                    traded.tradedMinET.text.toString().trim(),
                    traded.tradedMaxET.text.toString().trim(),
                    insider.officerCheBox.isChecked,
                    insider.directorCheBox.isChecked,
                    insider.owner10CheBox.isChecked,
                    sorting.groupSpinner.selectedItemPosition,
                    sorting.sortSpinner.selectedItemPosition
                )
                mainVM.saveSearchSet(set)

                (requireActivity() as MainActivity).hideKeyBoard(binding.general.tickerET)

                val bundle = Bundle().apply {
                    putString(ResultFragment.ARG_TITLE, getString(R.string.text_trading_screen))
                    putSerializable(ResultFragment.ARG_SEARCH_SET, set)
                }
                binding.searchButton.findNavController().navigate(R.id.nav_result, bundle)
            }
        }

        binding.alarmOffButton.setOnClickListener {
            val message = getString(R.string.text_cancel_search)
            ConfirmationDialog(message, "Ok", ConfirmationDialog.FLAG_CANCEL).apply {
                setOnClickListener(object : ConfirmationDialog.Listener {
                    override fun onPositiveClick(flag: String)
                    {
                        if (flag == ConfirmationDialog.FLAG_CANCEL)
                        {
                            requireActivity().let{ a ->
                                (a as MainActivity).setAlarmSetting(false)
                                AlarmPendingIntent.cancel(a)
                            }
                        }
                    }
                })
            }.show(parentFragmentManager, ConfirmationDialog.TAG)
        }

        StockInsiderService.serviceEvent.observe(viewLifecycleOwner, {
            if (!it.hasBeenHandled)
            {
                if (it.getContent() == StockInsiderService.STOP_KEY)
                {
                    context?.getString(R.string.text_search_is_disabled)?.let { msg ->
                        binding.alarmOffButton.visibility = View.GONE
                        Snackbar.make(binding.searchButton, msg, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        })

        binding.date.filingDateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long)
            {
                val array = requireContext().resources.getIntArray(R.array.value_for_filing_date)
                val selectedItem = array[p2]
                when
                {
                    selectedItem < 3 && selectedItem != 0 -> binding.date.tradeDateSpinner.setSelection(3)
                    selectedItem == 0 -> binding.date.tradeDateSpinner.setSelection(0)
                    else -> binding.date.tradeDateSpinner.setSelection(p2)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?)
            {

            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this){
            if ((requireActivity() as MainActivity).isNetworkConnectivity())
            {
                if (ad.isLoaded) ad.show() else (activity as MainActivity).finish()
                ad.adListener = object : AdListener(){
                    override fun onAdClosed() {
                        (activity as MainActivity).finish()
                    }
                }
            }
            else (activity as MainActivity).finish()
        }

    }

    override fun onPause()
    {
        with(binding) {
            val set = RoomSearchSet(
                searchName,
                "",
                general.tickerET.text.toString(),
                date.filingDateSpinner.selectedItemPosition,
                date.tradeDateSpinner.selectedItemPosition,
                traded.purchaseCheckBox.isChecked,
                traded.saleCheckBox.isChecked,
                traded.tradedMinET.text.toString().trim(),
                traded.tradedMaxET.text.toString().trim(),
                insider.officerCheBox.isChecked,
                insider.directorCheBox.isChecked,
                insider.owner10CheBox.isChecked,
                sorting.groupSpinner.selectedItemPosition,
                sorting.sortSpinner.selectedItemPosition
            )
            mainVM.setState(MainViewModel.State.Initial(set))
            //mainViewModel.saveSearchSet(set)
        }
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId)
        {
            R.id.action_default_search ->
            {
                val searchName = getString(R.string.text_default_set_name)
                mainVM.getCurrentSearchSet(searchName).observe(viewLifecycleOwner, {
                    mainVM.setState(MainViewModel.State.Initial(it))
                })
            }
            R.id.action_my_search ->
            {
                mainVM.getSearchSetList().observe(viewLifecycleOwner, { list ->
                    SearchListDialog.newInstance(list as MutableList<RoomSearchSet>, object : SearchListDialog.Listener {
                        override fun onSearchDialogPositiveClick(roomSearchSet: RoomSearchSet)
                        {
                            mainVM.setState(MainViewModel.State.Initial(roomSearchSet))
                        }
                        override fun onSearchDialogDeleteClick(roomSearchSet: RoomSearchSet)
                        {
                            mainVM.deleteSearchSet(roomSearchSet)
                        }
                    }).show(requireActivity().supportFragmentManager, SearchListDialog.TAG)
                })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy()
    {
        val value = mainVM.state.value
        value?.let { state ->
            if (state is MainViewModel.State.Initial)
            {
                val set = state.set
                mainVM.saveSearchSet(set)
            }
        }

        super.onDestroy()
    }


}