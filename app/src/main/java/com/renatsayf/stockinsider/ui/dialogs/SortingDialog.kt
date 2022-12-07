package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.DialogSortingBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SortingDialog : DialogFragment() {

    companion object {
        val TAG = "${this::class.java.simpleName}.TAG"

        private var dialog: SortingDialog? = null
        private var listener: Listener? = null

        fun instance(listener: Listener): SortingDialog {

            this.listener = listener
            return if (dialog == null) SortingDialog() else dialog!!
        }
    }

    private var binding: DialogSortingBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogSortingBinding.inflate(layoutInflater)
        return AlertDialog.Builder(requireContext()).apply {
            setView(binding!!.root)
        }.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_dialog_default)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding?.root ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let { b ->

        }
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }

    interface Listener {

    }
}