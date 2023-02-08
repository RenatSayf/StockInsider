package com.renatsayf.stockinsider.ui.main

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentHomeBinding
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.receivers.AlarmReceiver
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.dialogs.SearchListDialog
import com.renatsayf.stockinsider.ui.dialogs.WebViewDialog
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.ui.settings.Constants
import com.renatsayf.stockinsider.ui.tracking.list.TrackingListViewModel
import com.renatsayf.stockinsider.utils.appPref
import com.renatsayf.stockinsider.utils.hideKeyBoard
import com.renatsayf.stockinsider.utils.setAlarm
import com.renatsayf.stockinsider.utils.showAd
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val mainVM : MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java].apply {
            getSearchSetByName(getString(R.string.text_current_set_name)).observe(this@MainFragment) {
                this.setState(MainViewModel.State.Initial(it))
            }
        }
    }

    private val trackedVM: TrackingListViewModel by lazy {
        ViewModelProvider(this)[TrackingListViewModel::class.java]
    }

    private val tickersAdapter: TickersListAdapter by lazy {
        TickersListAdapter(requireContext())
    }

    private val googleAd1: InterstitialAd? by lazy {
        (activity as? MainActivity)?.googleAd1
    }
    private val yandexAd1: com.yandex.mobile.ads.interstitial.InterstitialAd? by lazy {
        (activity as? MainActivity)?.yandexAd1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        val isAgree = appPref.getBoolean(MainActivity.KEY_IS_AGREE, false)
        if (!isAgree) WebViewDialog().show(requireActivity().supportFragmentManager, WebViewDialog.TAG)

        mainVM.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MainViewModel.State.Initial -> {
                    val set = state.set
                    mainVM.setSearchSet(set)
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
                    hideKeyBoard(binding.root)
                    binding.general.tickerET.clearFocus()
                    binding.searchButton.requestFocus()
                }
            }
        }

        mainVM.companies.observe(viewLifecycleOwner) { companies ->
            companies?.let {
                tickersAdapter.addItems(it.toList())
            }
            binding.general.tickerET.setAdapter(tickersAdapter)
            binding.general.tickerET.clearFocus()
            //hideKeyBoard(binding.root)
        }

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

            val set = scanScreen()
            (requireActivity() as MainActivity).hideKeyBoard(it)

            val bundle = Bundle().apply {
                putString(ResultFragment.ARG_TITLE, getString(R.string.text_trading_screen))
                putSerializable(ResultFragment.ARG_SEARCH_SET, set)
            }
            findNavController().navigate(R.id.nav_result, bundle)
        }

        binding.date.filingDateSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val array = requireContext().resources.getIntArray(R.array.value_for_filing_date)
                    val selectedItem = array[p2]
                    when {
                        selectedItem < 3 && selectedItem != 0 -> binding.date.tradeDateSpinner.setSelection(3)
                        selectedItem == 0 -> binding.date.tradeDateSpinner.setSelection(0)
                        else -> binding.date.tradeDateSpinner.setSelection(p2)
                    }
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }

        binding.toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId)
                {
                    R.id.action_default_search ->
                    {
                        val searchName = getString(R.string.text_default_set_name)
                        mainVM.getSearchSetByName(searchName).observe(viewLifecycleOwner) {
                            mainVM.setState(MainViewModel.State.Initial(it))
                        }
                    }
                    R.id.action_my_search ->
                    {
                        mainVM.getSearchSetList().observe(viewLifecycleOwner) { list ->
                            SearchListDialog.newInstance(list as MutableList<RoomSearchSet>, object : SearchListDialog.Listener {
                                override fun onSearchDialogPositiveClick(roomSearchSet: RoomSearchSet) {
                                    mainVM.setState(MainViewModel.State.Initial(roomSearchSet))
                                }

                                override fun onSearchDialogDeleteClick(roomSearchSet: RoomSearchSet) {
                                    mainVM.deleteSearchSet(roomSearchSet)
                                }
                            }).show(requireActivity().supportFragmentManager, SearchListDialog.TAG)
                        }
                    }
                }
                return false
            }

        }, viewLifecycleOwner)

        binding.toolbar.setNavigationOnClickListener {
            val drawerLayout = (requireActivity() as MainActivity).drawerLayout
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                trackedVM.trackedCount().observe(viewLifecycleOwner) { count ->
                    count?.let {
                        if (it > 0) {
                            setAlarm(
                                scheduler = Scheduler(requireContext(), AlarmReceiver::class.java),
                                periodInMinute = Constants.WORK_PERIOD_IN_MINUTE
                            )
                        }
                    }
                }
                showAd(googleAd1, yandexAd1) {
                    requireActivity().finish()
                }
            }
        })
    }

    override fun onPause() {
        (requireActivity() as MainActivity).drawerLayout.isEnabled = false
        val set = scanScreen()
        set.queryName = getString(R.string.text_current_set_name)
        mainVM.saveSearchSet(set)
        mainVM.setState(MainViewModel.State.Initial(set))
        super.onPause()
    }

    private fun scanScreen(): RoomSearchSet {

        with(binding) {
            return RoomSearchSet(
                queryName = getString(R.string.text_current_set_name),
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
        }
    }


}