package com.renatsayf.stockinsider.ui.deal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Deal

class DealFragment : Fragment()
{

    companion object
    {
        val TAG = "deal_fragment"
    }

    private lateinit var viewModel : DealViewModel

    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View?
    {
        return inflater.inflate(R.layout.fragment_deal, container, false)
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DealViewModel::class.java)
        val deal = arguments?.get(TAG) as Deal
    }

}
