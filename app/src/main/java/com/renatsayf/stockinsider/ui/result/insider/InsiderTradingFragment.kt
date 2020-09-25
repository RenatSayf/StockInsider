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
import com.renatsayf.stockinsider.utils.AppLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.android.synthetic.main.no_result_layout.*
import javax.inject.Inject

@AndroidEntryPoint
class InsiderTradingFragment : Fragment()
{
    @Inject
    lateinit var appLog: AppLog

    companion object
    {
        val TAG = this::class.java.canonicalName.toString()
        val ARG_INSIDER_DEALS = this::class.java.canonicalName.toString()
        fun newInstance() = InsiderTradingFragment()
    }

    private lateinit var viewModel: InsiderTradingViewModel

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
        viewModel = ViewModelProvider(this)[InsiderTradingViewModel::class.java]

        val dealList = arguments?.getParcelableArrayList<Deal>(ARG_INSIDER_DEALS)

        alertLayout.visibility = View.GONE

        appLog.print(TAG,"****************** ${dealList?.size} ***********************")
        if (!dealList.isNullOrEmpty())
        {
            noResultLayout.visibility = View.GONE
            resultTV.text = dealList.size.toString()

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