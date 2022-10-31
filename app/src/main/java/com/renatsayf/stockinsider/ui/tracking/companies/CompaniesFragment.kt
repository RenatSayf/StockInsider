package com.renatsayf.stockinsider.ui.tracking.companies

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.CompaniesFragmentBinding
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.ui.adapters.CompanyListAdapter
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.tracking.TrackingListViewModel
import com.renatsayf.stockinsider.utils.setVisible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CompaniesFragment : Fragment(R.layout.companies_fragment), CompanyListAdapter.Listener {

    companion object {

        val ARG_TICKERS_LIST = "${this::class.java.simpleName}.tickersList"
        val ARG_TICKERS_STR = "${this::class.java.simpleName}.tickersStr"
        val ARG_SET_ID = "${this::class.java.simpleName}.setId"

        fun newInstance() = CompaniesFragment()
    }

    private lateinit var binding: CompaniesFragmentBinding
    private val companiesVM: CompaniesViewModel by activityViewModels()
    private val trackingVM: TrackingListViewModel by activityViewModels()
    private val mainVM: MainViewModel by viewModels()

    private val companiesAdapter = CompanyListAdapter(this)
    private val setId: String by lazy {
        this.arguments?.getString(ARG_SET_ID) ?: "Unnamed"
    }
    private val tickers: ArrayList<String>? by lazy {
        this.arguments?.getStringArrayList(ARG_TICKERS_LIST)
    }
    private val tickersStr: String? by lazy {
        this.arguments?.getString(ARG_TICKERS_STR)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.companies_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = CompaniesFragmentBinding.bind(view)

        companiesVM.state.observe(viewLifecycleOwner) { state ->
            when(state) {
                is CompaniesViewModel.State.Initial -> {
                    binding.tickerET.text.clear()
                    binding.tickerET.visibility = View.GONE
                    binding.btnAdd.visibility = View.VISIBLE
                }
                is CompaniesViewModel.State.OnAdding -> {
                    binding.tickerET.visibility = View.VISIBLE
                    binding.btnAdd.visibility = View.GONE
                }
                is CompaniesViewModel.State.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    binding.tickerET.text.clear()
                    binding.tickerET.visibility = View.GONE
                    binding.btnAdd.visibility = View.VISIBLE
                }
                is CompaniesViewModel.State.OnUpdate -> {
                    companiesAdapter.submitList(state.companies)
                }
            }
        }
        trackingVM.state.observe(viewLifecycleOwner) { state ->
            when(state) {
                is TrackingListViewModel.State.Edit -> {
                    enableEditing(state.flag)
                }
                is TrackingListViewModel.State.Initial -> {

                }
                is TrackingListViewModel.State.OnEdit -> {

                }
                is TrackingListViewModel.State.OnSave -> {}
            }
        }

        with(binding) {

            appToolbar.setNavigationOnClickListener {
                requireActivity().findNavController(R.id.nav_host_fragment).popBackStack()
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

            tickerET.doOnTextChanged { text, start, before, count ->
                if (!text.isNullOrEmpty()) {
                    companiesVM.getAllSimilarCompanies(text.toString()).observe(viewLifecycleOwner) { list ->
                        if (list != null) {
                            tickerListAdapter.addItems(list)
                            tickerET.showDropDown()
                        }
                    }
                }
            }
            tickerET.setOnItemClickListener { adapterView, view, i, l ->
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
                    companiesAdapter.submitList(companies)
                    tickerET.text.clear()
                }
                else companiesVM.setState(CompaniesViewModel.State.Error(getString(R.string.text_ticker_already_exists)))
            }
        }

        if (savedInstanceState == null) {
            tickers?.let { tList ->
                companiesVM.getCompaniesByTicker(tList).observe(viewLifecycleOwner) { list ->
                    list?.let { companies ->
                        companiesVM.setState(CompaniesViewModel.State.OnUpdate(companies))
                    }
                }
            }
        }

    }

    override fun onPause() {

        val tickerStr = companiesAdapter.items.joinToString(separator = "") {
            "${it.ticker} "
        }.trim()
        companiesVM.setState(CompaniesViewModel.State.Initial(tickerStr))
        super.onPause()
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