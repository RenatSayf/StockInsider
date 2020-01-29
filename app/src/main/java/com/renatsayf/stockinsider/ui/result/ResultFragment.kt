package com.renatsayf.stockinsider.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.DataTransferModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.android.synthetic.main.no_result_layout.*
import kotlinx.android.synthetic.main.no_result_layout.view.*

class ResultFragment : Fragment()
{

    companion object
    {
        val TAG: String? = "result_fragment"
    }

    private lateinit var viewModel : ResultViewModel
    private lateinit var dataTransferModel : DataTransferModel


    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View?
    {
        val root = inflater.inflate(R.layout.fragment_result, container, false)
        root.noResultLayout.visibility = View.GONE
        return root
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ResultViewModel::class.java)
        dataTransferModel = activity?.run {
            ViewModelProvider(activity as MainActivity)[DataTransferModel::class.java]
        }!!

        dataTransferModel.getSearchSet().observe(viewLifecycleOwner, Observer {
            val searchSet = it
            return@Observer
        })

        val dealList = arguments?.getParcelableArrayList<Deal>(TAG)
        dealList.let {
            if (it != null && it.size > 0)
            {
                resultTV.text = it.size.toString()
                val linearLayoutManager = LinearLayoutManager(activity)
                val dealListAdapter = DealListAdapter(activity as MainActivity, it)
                tradeListRV.apply {
                    setHasFixedSize(true)
                    layoutManager = linearLayoutManager
                    adapter = dealListAdapter
                }
                return@let
            }
            else
            {
                resultTV.text = it?.size.toString()
                noResultLayout.visibility = View.VISIBLE
            }
        }

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }
    }

}
