package com.renatsayf.stockinsider.donate

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.*
import com.renatsayf.stockinsider.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.donate_fragment.*

@AndroidEntryPoint
class DonateDialog : DialogFragment(), PurchasesUpdatedListener
{
    private var dialogView: View? = null
    private var skuList: MutableList<SkuDetails> = arrayListOf()

    companion object
    {
        val TAG = this::class.java.canonicalName
        private var instance: DonateDialog? = null
        fun getInstance() = if (instance == null)
        {
            DonateDialog()
        }
        else
        {
            instance as DonateDialog
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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View?
    {
        return dialogView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DonateViewModel::class.java)

        val thanksText = requireContext().getString(R.string.text_thanks).plus(" ")
            .plus(requireContext().getString(R.string.app_name))
        thanksTView.text = thanksText

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnDoDonate.setOnClickListener {

            dismiss()
        }

        viewModel.priceList.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty())
            {
                skuList = it
                val priceList = mutableListOf<String>()
                skuList.forEach {
                    priceList.add(it.price)
                }
                //priceList.sortDescending()
                val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        priceList
                )
                sumSpinnerView.adapter = adapter
            }
        })

        val billingClient = BillingClient.newBuilder(requireContext())
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener
                                      {
            override fun onBillingSetupFinished(result: BillingResult)
            {
                if (result.responseCode == BillingClient.BillingResponseCode.OK)
                {
                    viewModel.querySkuDetails(billingClient)
                }
            }

            override fun onBillingServiceDisconnected()
            {
                return
            }

        })

    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?)
    {

    }

}