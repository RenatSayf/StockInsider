package com.renatsayf.stockinsider.ui.tracking.companies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
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
import com.renatsayf.stockinsider.ui.tracking.item.TrackingFragment
import com.renatsayf.stockinsider.utils.KEY_FRAGMENT_RESULT
import com.renatsayf.stockinsider.utils.setVisible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CompaniesFragment : Fragment(R.layout.companies_fragment), CompanyListAdapter.Listener {

    companion object {
        const val ARG_TICKERS = "ARG_TICKERS"
    }

    private lateinit var binding: CompaniesFragmentBinding
    private val companiesVM: CompaniesViewModel by activityViewModels()

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
                    val editingFlag = arguments?.getBoolean(TrackingFragment.ARG_IS_EDIT, false)
                    enableEditing(editingFlag)
                }
            }
        }

        with(binding) {

            appToolbar.setNavigationOnClickListener {
                val companyList = companiesAdapter.items
                passDataBack(companyList)
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

            tickerET.doOnTextChanged { text, _, _, count ->
                if (count > 0) {
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
                }
                else companiesVM.setState(CompaniesViewModel.State.Error(getString(R.string.text_ticker_already_exists)))
                tickerET.text.clear()
            }
        }

    }

    private fun passDataBack(companies: List<Company>) {
        val tickersString = companies.joinToString(separator = " ") { company ->
            company.ticker
        }
        setFragmentResult(KEY_FRAGMENT_RESULT, Bundle().apply {
            putString(ARG_TICKERS, tickersString)
        })
    }

    override fun onResume() {

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val companyList = companiesAdapter.items
                passDataBack(companyList)
                findNavController().popBackStack()
            }
        })

        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        (requireActivity() as MainActivity).supportActionBar?.show()
        super.onStop()
    }

    private fun enableEditing(flag: Boolean?) {
        with(binding) {

            val editingFlag = flag ?: false
            btnAdd.setVisible(editingFlag)
            companiesAdapter.setEnable(editingFlag)
        }
    }

    override fun onItemClick(company: Company) {}

    override fun onItemRemoved(company: Company) {
        val companyList = companiesAdapter.items.toMutableList()
        companyList.remove(company)
        companiesVM.setState(CompaniesViewModel.State.OnUpdate(companyList))
    }


}