package com.renatsayf.stockinsider.ui.latest.purchases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.android.synthetic.main.load_progress_layout.*
import kotlinx.android.synthetic.main.no_result_layout.*

@AndroidEntryPoint
class Purchases1Fragment : Fragment()
{

    companion object
    {
        val ARG_SET_NAME = this::class.java.canonicalName.toString().plus("arg_set_name")
        const val PURCHASES_1_FOR_WEEK = "Purchases more 1 million"
        fun newInstance() = Purchases1Fragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            (activity as MainActivity).navController.navigate(R.id.nav_home)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        noResultLayout.visibility = View.GONE

        val setName = arguments?.getString(ARG_SET_NAME)
        if (!setName.isNullOrEmpty())
        {
            requireActivity().toolbar.title = "Purchases more \$1 million"
            val set = viewModel.getSearchSet(setName)
            val searchSet = SearchSet(set.setName).apply {
                ticker = set.ticker
                filingPeriod = set.filingPeriod.toString()
                tradePeriod = set.tradePeriod.toString()
                isPurchase = if (set.isPurchase) "1" else ""
                isSale = if (set.isSale) "1" else ""
                tradedMin = set.tradedMin
                tradedMax = set.tradedMax
                isOfficer = if (set.isOfficer) "1" else ""
                isDirector = if (set.isDirector) "1" else ""
                isTenPercent = if (set.isTenPercent) "1" else ""
                groupBy = set.groupBy.toString()
                sortBy = set.sortBy.toString()
            }
            arguments?.clear()
            requireActivity().loadProgreesBar.visibility = View.VISIBLE
            viewModel.getDealList(searchSet)
        }

        viewModel.dealList.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty())
            {
                resultTV.text = it.size.toString()
                val linearLayoutManager = LinearLayoutManager(activity)
                val dealListAdapter = DealListAdapter(it)
                dealListAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                tradeListRV.apply {
                    setHasFixedSize(true)
                    layoutManager = linearLayoutManager
                    adapter = dealListAdapter
                }
            }
            else
            {
                noResultLayout.visibility = View.VISIBLE
            }
            requireActivity().loadProgreesBar.visibility = View.GONE
        })
    }

}