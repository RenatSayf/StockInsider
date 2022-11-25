package com.renatsayf.stockinsider.ui.donate

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.android.billingclient.api.*
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.DonateFragmentBinding
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class DonateDialog : DialogFragment(), PurchasesUpdatedListener
{
    private lateinit var binding: DonateFragmentBinding
    private var products: MutableList<ProductDetails> = arrayListOf()
    private lateinit var billingClient: BillingClient


    companion object {
        val TAG = "${this::class.java.simpleName}.TAG"
        private var instance: DonateDialog? = null
        fun getInstance() = if (instance == null) {
            DonateDialog()
        } else {
            instance as DonateDialog
        }
    }

    private val viewModel: DonateViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        binding = DonateFragmentBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)
        return builder.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_dialog_default)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    viewModel.querySkuDetails(billingClient)
                }
            }
            override fun onBillingServiceDisconnected() {
                return
            }
        })

        viewModel.donateList?.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                products = list.toMutableList()
                val priceList = mutableListOf<String>()
                products.forEach { p ->
                    priceList.add(p.description)
                }
                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.app_spinner_item,
                    priceList
                )
                binding.sumSpinnerView.adapter = adapter
            }
        }

//        viewModel.priceList.observe(viewLifecycleOwner) {
//            if (!it.isNullOrEmpty()) {
//                products = it
//                val priceList = mutableListOf<String>()
//                products.forEach { sku ->
//                    priceList.add(sku.originalPrice)
//                }
//                val adapter = ArrayAdapter(
//                    requireContext(),
//                    R.layout.app_spinner_item,
//                    priceList
//                )
//                binding.sumSpinnerView.adapter = adapter
//            }
//        }

        val thanksText = requireContext().getString(R.string.text_thanks).plus(" ")
            .plus(requireContext().getString(R.string.app_name))
        binding.thanksTView.text = thanksText

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

//TODO Требуется дороботка https://developer.android.com/google/play/billing/migrate-gpblv5#initializing-billing:~:text=%2C%20billingFlowParams)-,After,-Kotlin
//        binding.btnDoDonate.setOnClickListener {
//            val selectedPrice = binding.sumSpinnerView.selectedItem.toString()
//            if(!products.isNullOrEmpty())
//            {
//                products.forEach {
//                    if (it.description == selectedPrice)
//                    {
//                        val flowParams = BillingFlowParams.newBuilder().setSkuDetails(it).build()
//                        billingClient.launchBillingFlow(requireActivity(), flowParams)
//                        return@forEach
//                    }
//                }
//            }
//        }

        viewModel.eventPurchased.observe(viewLifecycleOwner) {
            if (!it.hasBeenHandled) {
                Snackbar.make(
                    (requireActivity() as MainActivity).findViewById(R.id.main_content_layout),
                    getString(R.string.text_thanks_for_donating),
                    Snackbar.LENGTH_LONG
                ).show()
                dismiss()
            }
        }
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
            Snackbar.make((requireActivity() as MainActivity).findViewById(R.id.main_content_layout), getString(R.string.text_purchase_canceled), Snackbar.LENGTH_LONG).show()
            dismiss()
        }
    }

}