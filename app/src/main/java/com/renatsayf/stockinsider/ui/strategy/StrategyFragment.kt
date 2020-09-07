package com.renatsayf.stockinsider.ui.strategy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.renatsayf.stockinsider.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StrategyFragment : Fragment() {

    private lateinit var strategyViewModel: StrategyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        strategyViewModel = ViewModelProvider(this).get(StrategyViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_strategy, container, false)
        val textView: TextView = root.findViewById(R.id.text_tools)
        strategyViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}