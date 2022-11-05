package com.renatsayf.stockinsider.ui.tracking.companies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.CompaniesFragmentBinding
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.ui.adapters.CompanyListAdapter
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.tracking.item.TrackingViewModel
import com.renatsayf.stockinsider.utils.setVisible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CompaniesFragment : Fragment(R.layout.companies_fragment), CompanyListAdapter.Listener {

    private lateinit var binding: CompaniesFragmentBinding
    private val companiesVM: CompaniesViewModel by activityViewModels()
    private val trackingVM: TrackingViewModel by activityViewModels()

    private val companiesAdapter = CompanyListAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.companies_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = CompaniesFragmentBinding.bind(view)

        if (savedInstanceState == null) {
            val state = companiesVM.state.value
            if (state is CompaniesViewModel.State.Initial) {
                if (state.ticker.isNotEmpty()) {
                    val tickers = state.ticker.split(" ")
                    companiesVM.getCompaniesByTicker(tickers).observe(viewLifecycleOwner) { list ->
                        list?.let { companies ->
                            companiesVM.setState(CompaniesViewModel.State.Current(companies))
                        }
                    }
                } else binding.includeProgress.loadProgressBar.setVisible(false)
            }
        }

        companiesVM.state.observe(viewLifecycleOwner) { state ->
            when(state) {
                is CompaniesViewModel.State.Initial -> {
                    binding.tickerET.text.clear()
                    binding.tickerET.setVisible(false)
                    binding.btnAdd.setVisible(true)
                }
                is CompaniesViewModel.State.OnAdding -> {
                    binding.tickerET.setVisible(true)
                    binding.btnAdd.setVisible(false)
                }
                is CompaniesViewModel.State.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    binding.tickerET.text.clear()
                    binding.tickerET.setVisible(false)
                    binding.btnAdd.setVisible(true)
                    binding.includeProgress.loadProgressBar.setVisible(false)
                }
                is CompaniesViewModel.State.OnUpdate -> {
                    companiesAdapter.submitList(state.companies)
                }
                is CompaniesViewModel.State.Current -> {
                    companiesAdapter.submitList(state.companies)
                    binding.includeProgress.loadProgressBar.setVisible(false)
                }
            }
        }
        trackingVM.state.observe(viewLifecycleOwner) { state ->
            when(state) {
                is TrackingViewModel.State.Edit -> {
                    enableEditing(state.flag)
                }
                else -> {}
            }
        }

        with(binding) {

            appToolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            companyRV.apply {
                adapter = companiesAdapter
                addItemDecoration(DividerItemDecoration(requireContext(), LinearLayout.VERTICAL))
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            btnAdd.setOnClickListener {
                companiesVM.setState(CompaniesViewModel.State.OnAdding)
            }

            val tickerListAdapter = TickersListAdapter(requireContext(), emptyArray())
            tickerET.setAdapter(tickerListAdapter)

            tickerET.doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrEmpty()) {
                    companiesVM.getAllSimilarCompanies(text.toString()).observe(viewLifecycleOwner) { list ->
                        if (list != null) {
                            tickerListAdapter.addItems(list)
                            tickerET.showDropDown()
                        }
                    }
                }
            }
            tickerET.setOnItemClickListener { _, view, _, _ ->
                val tickerLayout = TickerLayoutBinding.bind(view)
                val ticker = tickerLayout.tickerTV.text.toString()
                val isContains = companiesAdapter.items.any {
                    it.ticker == ticker
                }
                if (!isContains) {
                    val company = tickerLayout.companyNameTV.text.toString()
                    val companies = companiesAdapter.items.ifEmpty {
                        listOf()
                    }.toMutableList()
                    companies.add(Company(ticker, company))
                    companiesVM.setState(CompaniesViewModel.State.Current(companies))
                    trackingVM.newSet?.let{
                        val set = it.copy()
                        set.apply {
                            this.ticker += " $ticker"
                            this.ticker = this.ticker.trim()
                        }
                        trackingVM.setState(TrackingViewModel.State.OnEdit(set))
                    }
                    tickerET.text.clear()
                }
                else companiesVM.setState(CompaniesViewModel.State.Error(getString(R.string.text_ticker_already_exists)))
            }
        }

    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        (activity as MainActivity).supportActionBar?.show()
        super.onStop()
    }

    private fun enableEditing(flag: Boolean) {
        with(binding) {

            btnAdd.setVisible(flag)
            companiesAdapter.setEnable(flag)
        }
    }

    override fun onItemClick(company: Company) {}

    override fun onItemRemoved(company: Company) {
        val companyList = companiesAdapter.items.toMutableList()
        companyList.remove(company)
        companiesVM.setState(CompaniesViewModel.State.OnUpdate(companyList))
    }


}