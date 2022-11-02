package com.renatsayf.stockinsider.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.DialogQueryNameBinding
import com.renatsayf.stockinsider.db.RoomSearchSet

class SetNameDialog(
    private val set: RoomSearchSet,
    private val listener: Listener
) : DialogFragment() {

    companion object {
        val TAG = "${this.hashCode()}.TAG"
    }

    private lateinit var binding: DialogQueryNameBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_dialog_default)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogQueryNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            btnSave.setOnClickListener {
                val name = etSetName.text.toString()
                listener.onSetNameDialogPositiveClick(name)
                dismiss()
            }
            btnCancel.setOnClickListener {
                dismiss()
            }
            btnGenerate.setOnClickListener {
                val queryName = set.generateQueryName()
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