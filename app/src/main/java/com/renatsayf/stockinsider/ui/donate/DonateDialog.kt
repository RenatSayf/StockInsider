package com.renatsayf.stockinsider.ui.donate

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.DonateFragmentBinding
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.utils.goToUrl
import com.renatsayf.stockinsider.utils.setVisible
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
                val donateUrl = FireBaseConfig.donateUrl
                if (donateUrl.isNotEmpty()) {
                    requireContext().goToUrl(donateUrl)
                }
                dismiss()
            }
        }


    }

}