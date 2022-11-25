package com.renatsayf.stockinsider.ui.result.insider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentResultBinding
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.deal.DealFragment
import com.renatsayf.stockinsider.ui.deal.DealViewModel
import com.renatsayf.stockinsider.utils.setVisible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class InsiderTradingFragment : Fragment(R.layout.fragment_result), DealListAdapter.Listener {
    private lateinit var binding: FragmentResultBinding

    private val dealVM: DealViewModel by viewModels()

    private val dealsAdapter: DealListAdapter by lazy {
        DealListAdapter(this@InsiderTradingFragment)
    }

    companion object {
        val TAG = this::class.java.simpleName.toString()
        val ARG_TITLE = this::class.java.simpleName.toString().plus("title")
        val ARG_INSIDER_NAME = this::class.java.simpleName.toString().plus("insider_name")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentResultBinding.bind(view)

        with(binding) {

            val title = arguments?.getString(ARG_TITLE)
            val insiderName = arguments?.getString(ARG_INSIDER_NAME)

            tradeListRV.apply {
                setHasFixedSize(true)
                adapter = dealsAdapter.apply {
                    showSkeleton()
                }
            }

            insiderNameLayout.setVisible(true)
            includedProgress.setVisible(true)
            noResult.root.setVisible(false)

            insiderName?.let { name ->
                dealVM.getInsiderDeals(name).observe(viewLifecycleOwner) { list ->
                    includedProgress.setVisible(false)
                    if (list.isNotEmpty()) {
                        noResult.root.setVisible(false)
                        btnAddToTracking.setVisible(true)
                        resultTV.text = list.size.toString()
                        titleTView.text = title
                        insiderNameTView.text = list[0].insiderName

                        dealsAdapter.apply {
                            addItems(list)
                        }
                    }
                    else {
                        insiderNameLayout.setVisible(false)
                        includedProgress.setVisible(false)
                        noResult.root.setVisible(true)
                        btnAddToTracking.setVisible(false)
                    }
                }
            } ?: run {
                insiderNameLayout.setVisible(false)
                includedProgress.setVisible(false)
                noResult.root.setVisible(true)
                btnAddToTracking.setVisible(false)
            }

            btnAddToTracking.setOnClickListener {

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

}