package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.DialogSortingBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SortingDialog : DialogFragment() {

    companion object {
        val TAG = "${this::class.java.simpleName}.TAG"

        private var dialog: SortingDialog? = null
        fun instance(): SortingDialog {
            return if (dialog == null) SortingDialog() else dialog!!
        }

    }

    private lateinit var binding: DialogSortingBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogSortingBinding.inflate(layoutInflater)
        return AlertDialog.Builder(requireContext()).apply {
            setView(binding.root)
        }.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_dialog_default)
        }
    }
}