package com.renatsayf.stockinsider.donate

import android.app.Dialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.renatsayf.stockinsider.R

class DonateFragment : DialogFragment()
{

    companion object
    {
        fun newInstance() = DonateFragment()
    }

    private lateinit var viewModel: DonateViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.donate_fragment, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DonateViewModel::class.java)
        // TODO: Use the ViewModel
    }

}