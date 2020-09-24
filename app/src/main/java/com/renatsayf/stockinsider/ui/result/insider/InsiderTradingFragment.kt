package com.renatsayf.stockinsider.ui.result.insider

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import kotlinx.android.synthetic.main.fragment_result.*

class InsiderTradingFragment : Fragment()
{

    companion object
    {
        val ARG_INSIDER_DEALS = this::class.java.canonicalName.toString()
        fun newInstance() = InsiderTradingFragment()
    }

    private lateinit var viewModel: InsiderTradingViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.insider_trading_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[InsiderTradingViewModel::class.java]

        val dealList = arguments?.getParcelableArrayList<Deal>(ARG_INSIDER_DEALS)

        println("****************** ${dealList?.size} ***********************")
        if (!dealList.isNullOrEmpty())
        {
            val linearLayoutManager = LinearLayoutManager(activity)
            val dealListAdapter = DealListAdapter(dealList)
            dealListAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            tradeListRV.apply {
                setHasFixedSize(true)
                layoutManager = linearLayoutManager
                adapter = dealListAdapter
            }
        }
    }

}