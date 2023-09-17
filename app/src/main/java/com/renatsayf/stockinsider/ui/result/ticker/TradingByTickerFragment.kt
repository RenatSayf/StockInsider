package com.renatsayf.stockinsider.ui.result.ticker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentResultBinding
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.deal.DealFragment
import com.renatsayf.stockinsider.ui.deal.DealViewModel
import com.renatsayf.stockinsider.ui.sorting.SortingViewModel
import com.renatsayf.stockinsider.utils.setVisible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TradingByTickerFragment : Fragment(R.layout.fragment_result), DealListAdapter.Listener {
    private lateinit var binding: FragmentResultBinding

    private val dealVM: DealViewModel by viewModels()
    private val sortingVM: SortingViewModel by viewModels()

    private val dealsAdapter by lazy {
        DealListAdapter(this)
    }

    companion object {
        val TAG = this::class.java.simpleName.toString()
        val ARG_TOOL_BAR_TITLE = "${this::class.java.simpleName}.ARG_TOOL_BAR_TITLE"
        val ARG_TICKER = this::class.java.simpleName.toString().plus(".ARG_TICKER")
        val ARG_TITLE = this::class.java.simpleName.toString().plus(".ARG_TITLE")
        val ARG_COMPANY_NAME = this::class.java.simpleName.toString().plus(".ARG_COMPANY_NAME")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentResultBinding.bind(view)
        with(binding) {

            btnSorting.setVisible(false)
            includedProgress.setVisible(true)
            insiderNameLayout.setVisible(true)
            noResult.root.setVisible(false)

            tradeListRV.apply {
                setHasFixedSize(true)
                adapter = dealsAdapter.apply {
                    showSkeleton()
                }
            }

            toolBar.apply {
                title = arguments?.getString(ARG_TOOL_BAR_TITLE)
                setNavigationOnClickListener {
                    findNavController().popBackStack()
                }
            }
            val title = arguments?.getString(ARG_TITLE)
            val companyName = arguments?.getString(ARG_COMPANY_NAME)
            val ticker = arguments?.getString(ARG_TICKER)

            if (savedInstanceState == null && ticker != null) {
                dealVM.getDealsByTicker(ticker)
            }

            dealVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    is DealViewModel.State.OnData -> {
                        includedProgress.setVisible(false)
                        val list = state.data
                        if (list.isNotEmpty()) {
                            noResult.noResultLayout.setVisible(false)
                            includedProgress.setVisible(false)
                            btnAddToTracking.setVisible(false)
                            resultTV.text = list.size.toString()
                            titleTView.text = title
                            insiderNameTView.text = companyName
                            val map = sortingVM.doSort(list, sortingVM.sorting)
                            dealsAdapter.replaceItems(map, sortingVM.sorting)
                        }
                        else {
                            insiderNameLayout.setVisible(false)
                            includedProgress.setVisible(false)
                            noResult.root.setVisible(true)
                            btnAddToTracking.setVisible(false)
                        }
                    }
                    is DealViewModel.State.OnError -> {
                        insiderNameLayout.setVisible(false)
                        includedProgress.setVisible(false)
                        noResult.root.setVisible(true)
                        btnAddToTracking.setVisible(false)
                    }
                    DealViewModel.State.OnLoad -> {
                        includedProgress.setVisible(true)
                    }
                }
            }

            btnAddToTracking.setOnClickListener {

            }
            noResult.backButton.setOnClickListener {
                findNavController().popBackStack()
            }

        }

    }

    override fun onRecyclerViewItemClick(deal: Deal) {
        val bundle = Bundle().apply {
            putParcelable(DealFragment.ARG_DEAL, deal)
            putString(DealFragment.ARG_TITLE, deal.company)
        }
        findNavController().navigate(R.id.nav_deal, bundle)
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).supportActionBar?.hide()
    }

    override fun onStop() {

        (requireActivity() as MainActivity).supportActionBar?.show()
        super.onStop()
    }

}