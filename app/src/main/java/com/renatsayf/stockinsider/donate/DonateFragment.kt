package com.renatsayf.stockinsider.donate

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.renatsayf.stockinsider.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DonateFragment : DialogFragment()
{
    private var dialogView: View? = null

    companion object
    {
        val TAG = this::class.java.canonicalName
        private var instance: DonateFragment? = null
        fun getInstance() = if (instance == null)
        {
            DonateFragment()
        }
        else
        {
            instance as DonateFragment
        }
    }

    private lateinit var viewModel: DonateViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.donate_fragment, ConstraintLayout(requireContext()), false)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogView)
        return builder.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.rectangle_frame_background)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DonateViewModel::class.java)

    }

}