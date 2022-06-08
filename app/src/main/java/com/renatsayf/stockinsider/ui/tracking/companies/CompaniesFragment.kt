package com.renatsayf.stockinsider.ui.tracking.companies

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.CompaniesFragmentBinding
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.ui.adapters.CompanyListAdapter
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CompaniesFragment : Fragment(R.layout.companies_fragment) {

    companion object {

        val ARG_TICKERS_LIST = "${this::class.java.simpleName}.tickersList"
        val ARG_TICKERS_STR = "${this::class.java.simpleName}.tickersStr"
        val ARG_SET_ID = "${this::class.java.simpleName}.setId"

        fun newInstance() = CompaniesFragment()
    }

    private lateinit var binding: CompaniesFragmentBinding
    private val companiesVM: CompaniesViewModel by lazy {
        ViewModelProvider(this)[CompaniesViewModel::class.java]
    }
    private val mainVM: MainViewModel by viewModels()

    private val companiesAdapter = CompanyListAdapter()
    private val setId: Int by lazy {
        this.arguments?.getInt(ARG_SET_ID) ?: -1
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
                val company = tickerLayout.companyNameTV.text.toString()
                val companies = listOf(Companies(ticker, company))
                companiesAdapter.addItems(companies)

                if (tickersStr?.contains(ticker) == false) {
                    val newTickersStr = tickersStr.plus(" ").plus(ticker)
                    companiesVM.addCompanyToSearch(setId, newTickersStr).observe(viewLifecycleOwner) {
                        if (it < 0) companiesVM.setState(CompaniesViewModel.State.Error("Failed to update database"))
                    }
                }
                else companiesVM.setState(CompaniesViewModel.State.Error(getString(R.string.text_ticker_already_exists)))
            }
        }

        if (savedInstanceState == null) {
            tickers?.let { tList ->
                companiesVM.getCompaniesByTicker(tList).observe(viewLifecycleOwner) { list ->
                    list?.let { companies ->
                        companiesAdapter.addItems(companies)
                    }
                }
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


}