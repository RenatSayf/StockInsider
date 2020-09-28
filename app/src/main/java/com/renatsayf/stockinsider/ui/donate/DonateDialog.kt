package com.renatsayf.stockinsider.ui.donate

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.*
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.donate_fragment.*

@AndroidEntryPoint
class DonateDialog : DialogFragment(), PurchasesUpdatedListener
{
    private var dialogView: View? = null
    private var skuList: MutableList<SkuDetails> = arrayListOf()
    private lateinit var billingClient: BillingClient

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
        viewModel = ViewModelProvider(this).get(DonateViewModel::class.java)
        return dialogView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        billingClient = BillingClient.newBuilder(requireContext())
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

        viewModel.priceList.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty())
            {
                skuList = it
                val priceList = mutableListOf<String>()
                skuList.forEach { sku ->
                    priceList.add(sku.originalPrice)
                }
                val adapter = ArrayAdapter(
                        requireContext(),
                        R.layout.app_spinner_item,
                        priceList
                )
                sumSpinnerView.adapter = adapter
            }
        })

        val thanksText = requireContext().getString(R.string.text_thanks).plus(" ")
            .plus(requireContext().getString(R.string.app_name))
        thanksTView.text = thanksText

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnDoDonate.setOnClickListener {
            val selectedPrice = sumSpinnerView.selectedItem.toString()
            if(!skuList.isNullOrEmpty())
            {
                skuList.forEach {
                    if (it.originalPrice == selectedPrice)
                    {
                        val flowParams = BillingFlowParams.newBuilder().setSkuDetails(it).build()
                        billingClient.launchBillingFlow(requireActivity(), flowParams)
                        return@forEach
                    }
                }
            }
        }

        viewModel.eventPurchased.observe(viewLifecycleOwner, {
            if (!it.hasBeenHandled)
            {
                Snackbar.make((requireActivity() as MainActivity).expandMenu, getString(R.string.text_thanks_for_donating), Snackbar.LENGTH_LONG).show()
                dismiss()
            }
        })
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?)
    {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null)
        {
            for (purchase in purchases)
            {
                viewModel.handlePurchase(billingClient, purchase)
            }
        }
        else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED)
        {
            Snackbar.make((requireActivity() as MainActivity).expandMenu, getString(R.string.text_purchase_caceled), Snackbar.LENGTH_LONG).show()
            dismiss()
        }
    }

}