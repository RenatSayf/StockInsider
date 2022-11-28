package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ConfirmationDialog : DialogFragment()
{
    companion object{
        val TAG = this::class.java.simpleName.plus("_confirmation_dialog")
        private var listener: Listener? = null
        private var title: String = ""
        private var message: String = ""
        private var positiveButtonText = "Ok"

        fun newInstance(
            title: String = "",
            message: String = "",
            positiveButtonText: String = "Ok",
            listener: Listener
        ): ConfirmationDialog {
            this.title = title
            this.message = message
            this.positiveButtonText = positiveButtonText
            this.listener = listener
            return ConfirmationDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog
    {
        val builder = AlertDialog.Builder(activity)
        builder
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { _: DialogInterface?, _: Int ->
                listener?.onPositiveClick()
            }
            .setNegativeButton(getString(R.string.text_cancel)){ _, _ ->
                dismiss()
            }

        return builder.create()
    }

    interface Listener
    {
        fun onPositiveClick()
    }
}