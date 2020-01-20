package com.renatsayf.stockinsider.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.renatsayf.stockinsider.R
import org.jsoup.Jsoup

class ResultFragment : Fragment()
{

    companion object
    {
        val TAG: String? = "result_fragment"
    }

    private lateinit var viewModel : ResultViewModel

    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View?
    {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ResultViewModel::class.java)



        val strTable = arguments?.getString(TAG)
        strTable.let {
//            if (it?.size!! > 0)
//            {
//                val linearLayoutManager = LinearLayoutManager(activity)
//                val dealListAdapter = DealListAdapter(it)
//                tradeListRV.apply {
//                    setHasFixedSize(true)
//                    layoutManager = linearLayoutManager
//                    adapter = dealListAdapter
//                }
//
//                return@let
//            }
            val parseBodyFragment = Jsoup.parseBodyFragment(strTable)
            val document = Jsoup.parse(strTable)
            document
        }


    }

}
