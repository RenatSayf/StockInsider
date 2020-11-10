package com.renatsayf.stockinsider.ui.result.ticker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.result.insider.InsiderTradingFragment
import com.renatsayf.stockinsider.utils.AppLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.android.synthetic.main.no_result_layout.*
import javax.inject.Inject


@AndroidEntryPoint
class TradingByTickerFragment : Fragment()
{
    @Inject
    lateinit var appLog: AppLog

    companion object
    {
        val TAG = this::class.java.canonicalName.toString()
        val ARG_TICKER_DEALS = this::class.java.canonicalName.toString().plus("deals_list")
        val ARG_TITLE = this::class.java.canonicalName.toString().plus("title")
        val ARG_COMPANY_NAME = this::class.java.canonicalName.toString().plus("company_name")

        fun newInstance() = TradingByTickerFragment()
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

        val dealList = arguments?.getParcelableArrayList<Deal>(ARG_TICKER_DEALS)
        val title = arguments?.getString(ARG_TITLE)
        val companyName = arguments?.getString(ARG_COMPANY_NAME)

        alertLayout.visibility = View.GONE
        insiderNameLayout.visibility = View.VISIBLE

        appLog.print(InsiderTradingFragment.TAG, "****************** ${dealList?.size} ***********************")
        if (!dealList.isNullOrEmpty())
        {
            noResultLayout.visibility = View.GONE
            resultTV.text = dealList.size.toString()
            titleTView.text = title
            insiderNameTView.text = companyName

            val linearLayoutManager = LinearLayoutManager(activity)
            val dealListAdapter = DealListAdapter(dealList, R.layout.deal_layout)
            dealListAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            tradeListRV.apply {
                setHasFixedSize(true)
                layoutManager = linearLayoutManager
                adapter = dealListAdapter
            }
        }
    }

}