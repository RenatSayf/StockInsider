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
import com.android.billingclient.api.BillingClient
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.DonateFragmentBinding
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.ui.settings.isAdsDisabled
import com.renatsayf.stockinsider.utils.goToUrl
import com.renatsayf.stockinsider.utils.setVisible
import com.renatsayf.stockinsider.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DonateDialog : DialogFragment()
{
    private lateinit var binding: DonateFragmentBinding

    companion object {
        val TAG = "${this::class.java.simpleName}.TAG"
        fun getInstance(): DonateDialog {
            return DonateDialog()
        }
    }

    private val viewModel: DonateViewModel by activityViewModels()
    private val isBillingEnabled: Boolean by lazy {
        FireBaseConfig.isBillingEnabled
    }

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

        with(binding) {
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
                    sumSpinnerView.adapter = adapter
                }
            }

            if (isBillingEnabled) {
                thanksTView.setVisible(true)
                selectSumTView.setVisible(true)
                sumSpinnerView.setVisible(true)
            }

            val donateText = "${getString(R.string.hi_emoji)} ${getString(R.string.text_donate)}"
            doDonateTView.text = donateText

            val thanksText = requireContext().getString(R.string.text_thanks).plus(" ")
                .plus(requireContext().getString(R.string.app_name))
            thanksTView.text = thanksText

            val cancelText = "${getString(R.string.unamused_emoji)}  ${getString(R.string.text_cancel)}"
            btnCancel.text = cancelText
            btnCancel.setOnClickListener {
                dismiss()
            }

            val doDonateText = "${getString(R.string.text_to_support)}  ${getString(R.string.hugging_emoji)}"
            btnDoDonate.text = doDonateText
            btnDoDonate.setOnClickListener {
                if (isBillingEnabled) {
                    val selectedPrice = sumSpinnerView.selectedItem as? String
                    if (selectedPrice != null) {
                        viewModel.buildBillingFlowParams(selectedPrice).observe(viewLifecycleOwner) { params ->
                            if (params != null) {
                                viewModel.billingClient.launchBillingFlow(requireActivity(), params)
                            }
                        }
                    }
                } else {
                    val donateUrl = FireBaseConfig.donateUrl
                    if (donateUrl.isNotEmpty()) {
                        requireContext().goToUrl(donateUrl)
                    }
                    dismiss()
                }
            }

            viewModel.donationIsDone.observe(viewLifecycleOwner) { result ->
                val layout = (activity as? MainActivity)?.drawerLayout
                result.onSuccess<String> {
                    requireContext().isAdsDisabled = true
                    layout?.showSnackBar("${getString(R.string.hugging_emoji)}  ${getString(R.string.text_thanks_for_donating)}")
                    dismiss()
                }
                result.onError { message, code ->
                    if (code == BillingClient.BillingResponseCode.USER_CANCELED) {
                        layout?.showSnackBar("${getString(R.string.unamused_emoji)}  ${getString(R.string.text_purchase_canceled)}")
                    }
                    else {
                        layout?.showSnackBar(message)
                    }
                    dismiss()
                }
            }
        }


    }

}