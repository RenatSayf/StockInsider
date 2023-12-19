package com.renatsayf.stockinsider.ui.donate

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.DonateFragmentBinding
import com.renatsayf.stockinsider.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DonateDialog : DialogFragment()
{
    private lateinit var binding: DonateFragmentBinding

    companion object {
        val TAG = "${this::class.java.simpleName}.TAG"
        private var instance: DonateDialog? = null
        fun getInstance() = if (instance == null) {
            DonateDialog()
        } else {
            instance as DonateDialog
        }
    }

    private val viewModel: DonateViewModel by activityViewModels()

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

        viewModel.donateList.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                val products = list.toMutableList()
                val priceList = mutableListOf<String>()
                products.forEach { p ->
                    val price = p.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                    priceList.add(price)
                }
                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.app_spinner_item,
                    priceList
                )
                binding.sumSpinnerView.adapter = adapter
            }
        }

        val thanksText = requireContext().getString(R.string.text_thanks).plus(" ")
            .plus(requireContext().getString(R.string.app_name))
        binding.thanksTView.text = thanksText

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnDoDonate.setOnClickListener {
            val selectedPrice = binding.sumSpinnerView.selectedItem as? String
            if (selectedPrice != null) {
                viewModel.buildBillingFlowParams(selectedPrice).observe(viewLifecycleOwner) { params ->
                    if (params != null) {
                        viewModel.billingClient.launchBillingFlow(requireActivity(), params)
                    }
                }
            }
        }

        viewModel.eventPurchased.observe(viewLifecycleOwner) { event ->
            if (!event.hasBeenHandled) {
                val layout = (activity as? MainActivity)?.drawerLayout
                if (!event.getContent().isNullOrEmpty()) {
                    layout?.showSnackBar(getString(R.string.text_thanks_for_donating))
                    dismiss()
                }
                else {
                    layout?.showSnackBar(getString(R.string.text_purchase_canceled))
                    dismiss()
                }
            }
        }
    }

}