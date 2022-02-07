package com.renatsayf.stockinsider.ui.tracking.companies

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.CompaniesFragmentBinding
import com.renatsayf.stockinsider.ui.adapters.CompanyListAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CompaniesFragment : Fragment(R.layout.companies_fragment) {

    companion object {

        val ARG_TICKERS_LIST = "${this::class.java.simpleName}.tickersList"

        fun newInstance() = CompaniesFragment()
    }

    private lateinit var binding: CompaniesFragmentBinding
    private val companiesVM: CompaniesViewModel by lazy {
        ViewModelProvider(this)[CompaniesViewModel::class.java]
    }

    private val companiesAdapter = CompanyListAdapter()

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
                    binding.tickerET.visibility = View.GONE
                    binding.btnAdd.visibility = View.VISIBLE
                }
                CompaniesViewModel.State.OnAdding -> {
                    binding.tickerET.visibility = View.VISIBLE
                    binding.btnAdd.visibility = View.GONE
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

        }

        arguments?.let {
            val tickers = it.getStringArrayList(ARG_TICKERS_LIST)
            if (savedInstanceState == null && tickers != null) {
                companiesVM.getCompaniesByTicker(tickers).observe(viewLifecycleOwner) { list ->
                    list?.let{ companies ->
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