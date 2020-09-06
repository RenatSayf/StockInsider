package com.renatsayf.stockinsider.ui.latest.purchases25

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R

class Purchases25Fragment : Fragment()
{

    companion object
    {
        fun newInstance() = Purchases25Fragment()
    }

    private lateinit var viewModel: Purchases25ViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            (activity as MainActivity).navController.navigate(R.id.nav_home)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.purchases25_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(Purchases25ViewModel::class.java)
        // TODO: Use the ViewModel
    }

}