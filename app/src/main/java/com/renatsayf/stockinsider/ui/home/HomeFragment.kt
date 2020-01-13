package com.renatsayf.stockinsider.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.renatsayf.stockinsider.R
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class HomeFragment : Fragment()
{

    private lateinit var homeViewModel : HomeViewModel

    override fun onCreateView(
            inflater : LayoutInflater,
            container : ViewGroup?,
            savedInstanceState : Bundle?
                             ) : View?
    {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)



        return root
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        val tickerArray = activity?.resources?.getStringArray(R.array.ticker_list)
        val tickerListAdapter = tickerArray?.let {
            ArrayAdapter<String>(
                    activity!!.applicationContext, R.layout.support_simple_spinner_dropdown_item, it
                                )
        }
        ticker_ET.setAdapter(tickerListAdapter)
        //ticker_ET.threshold = 1

        homeViewModel.ticker.observe(this, Observer {
            ticker_ET.setText(it)
        })
        ticker_ET.doOnTextChanged { text, start, count, after ->
            homeViewModel.setTicker(text.toString())
        }

        search_button.setOnClickListener {

                val async = GlobalScope.async {
                    val screen = fetchTradingScreen()
                    screen
                }

        }
    }

    suspend fun fetchTradingScreen() : String?
    {
//        val searchRequest = SearchRequest()
//        searchRequest.tickers = ticker_ET.text.toString()
//        val screen = searchRequest.fetchTradingScreen()
        return null
    }
}