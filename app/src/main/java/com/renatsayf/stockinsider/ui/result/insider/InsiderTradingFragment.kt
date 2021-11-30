package com.renatsayf.stockinsider.ui.result.insider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentResultBinding
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter


class InsiderTradingFragment : Fragment(R.layout.fragment_result)
{
    private lateinit var binding: FragmentResultBinding

    companion object
    {
        val TAG = this::class.java.simpleName.toString()
        val ARG_INSIDER_DEALS = this::class.java.simpleName.toString().plus("deals_list")
        val ARG_TITLE = this::class.java.simpleName.toString().plus("title")
        val ARG_INSIDER_NAME = this::class.java.simpleName.toString().plus("insider_name")
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentResultBinding.bind(view)

        val dealList = arguments?.getParcelableArrayList<Deal>(ARG_INSIDER_DEALS)
        val title = arguments?.getString(ARG_TITLE)
        val insiderName = arguments?.getString(ARG_INSIDER_NAME)

        binding.alertLayout.root.visibility = View.GONE
        binding.insiderNameLayout.visibility = View.VISIBLE

        if (!dealList.isNullOrEmpty())
        {
            binding.noResult.root.visibility = View.GONE
            binding.resultTV.text = dealList.size.toString()
            binding.titleTView.text = title
            binding.insiderNameTView.text = insiderName

            val linearLayoutManager = LinearLayoutManager(activity)
            val dealListAdapter = DealListAdapter(dealList)
            dealListAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            binding.tradeListRV.apply {
                setHasFixedSize(true)
                layoutManager = linearLayoutManager
                adapter = dealListAdapter
            }
        }
    }

}