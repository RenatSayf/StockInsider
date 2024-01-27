@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.DialogQueryNameBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.utils.setVisible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SetNameDialog : DialogFragment() {

    companion object {
        val TAG = "${this.hashCode()}.TAG"
        private var set: RoomSearchSet? = null
        private var listener: Listener? = null

        fun newInstance(set: RoomSearchSet, listener: Listener): SetNameDialog {

            this.set = set
            this.listener = listener
            return SetNameDialog()
        }
    }

    private lateinit var binding: DialogQueryNameBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogQueryNameBinding.inflate(layoutInflater)

        return AlertDialog.Builder(requireContext()).apply {
            setView(binding.root)
        }.create().apply {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            etSetName.setText(set?.queryName)
            etSetName.doOnTextChanged { text, start, before, count ->
                btnSave.setVisible(count > 0)
            }

            btnSave.setOnClickListener {
                val name = etSetName.text.toString()
                listener?.onSetNameDialogPositiveClick(name)
                dismiss()
            }
            btnCancel.setOnClickListener {
                dismiss()
            }
            btnGenerate.setOnClickListener {
                val queryName = set?.generateQueryName()
                etSetName.setText(queryName)
            }
            btnClear.setOnClickListener {
                etSetName.text.clear()
            }

        }
    }

    interface Listener {
        fun onSetNameDialogPositiveClick(name: String)
    }
}