package com.renatsayf.stockinsider.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentHomeBinding
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.dialogs.SearchListDialog
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.utils.hideKeyBoard
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainFragment : Fragment() {

    companion object {
        val TAG = "${MainFragment::class.simpleName}.1111"
    }

    private lateinit var binding: FragmentHomeBinding

    private val mainVM : MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java].apply {
            getSearchSetByName(getString(R.string.text_current_set_name)).observe(this@MainFragment) {
                this.setState(MainViewModel.State.Initial(it))
            }
        }
    }

    private val tickersAdapter: TickersListAdapter by lazy {
        TickersListAdapter(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainVM.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MainViewModel.State.Initial -> {
                    val set = state.set
                    with(binding) {
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
            set.queryName = getString(R.string.text_current_set_name)
            findNavController().navigate(R.id.nav_result, Bundle().apply {
                putSerializable(ResultFragment.ARG_SEARCH_SET, set)
            })
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
                findNavController().popBackStack()
            }
        })
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun scanScreen(): RoomSearchSet {

        with(binding) {

            val datePeriodValues = resources.getIntArray(R.array.value_for_filing_date)
            val fillingPeriod = datePeriodValues[date.filingDateSpinner.selectedItemPosition]
            val tradePeriod = datePeriodValues[date.tradeDateSpinner.selectedItemPosition]

            return RoomSearchSet(
                queryName = getString(R.string.text_current_set_name),
                "",
                ticker = general.tickerET.text.toString(),
                filingPeriod = fillingPeriod,
                tradePeriod = tradePeriod,
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