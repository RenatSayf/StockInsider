package com.renatsayf.stockinsider.ui.strategy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.DataTransferModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_strategy.*
import kotlinx.android.synthetic.main.fragment_strategy.view.*

@AndroidEntryPoint
class StrategyFragment : Fragment()
{
    companion object
    {
        private var instance: StrategyFragment? = null
        fun getInstance(): StrategyFragment = if (instance == null) StrategyFragment() else instance as StrategyFragment
    }

    private lateinit var strategyViewModel: StrategyViewModel
    private lateinit var dataTransferModel: DataTransferModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        dataTransferModel = ViewModelProvider(activity as MainActivity)[DataTransferModel::class.java]
        requireActivity().onBackPressedDispatcher.addCallback(this){
            (activity as MainActivity).navController.navigate(R.id.nav_home)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        strategyViewModel = ViewModelProvider(this).get(StrategyViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_strategy, container, false)
        root.apply {
            webView.loadUrl("file:///android_asset/strategy/index.html")
        }
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        webView.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean
            {
                p0?.performClick()
                return false
            }
        })

    }
}