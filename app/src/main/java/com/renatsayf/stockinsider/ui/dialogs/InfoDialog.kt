@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class InfoDialog private constructor() : DialogFragment() {

    companion object {
        val TAG = "${this::class.java.simpleName}.TAG"

        private var title: String = ""
        private var message: String = ""
        private var status: DialogStatus = DialogStatus.INFO
        private var instance: InfoDialog? = null

        fun newInstance(title: String = "", message: String, status: DialogStatus): InfoDialog {
            this.title = title
            this.message = message
            this.status = status
            return if (instance == null ) InfoDialog() else instance!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return AlertDialog.Builder(requireContext()).apply {
            when(status) {
                DialogStatus.SUCCESS -> setIcon(R.drawable.ic_circle_check_green)
                DialogStatus.INFO -> setIcon(R.drawable.ic_info_blue)
                DialogStatus.ERROR -> setIcon(R.drawable.ic_error_red)
                DialogStatus.WARNING -> setIcon(R.drawable.ic_warning_yellow)
            }
            if (title.isNotEmpty()) {
                setTitle(title)
            }
            if (message.isNotEmpty()) {
                setMessage(message)
            }
            setPositiveButton(getString(R.string.text_ok), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dismiss()
                }
            })
        }.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
        }
    }

    enum class DialogStatus {
        SUCCESS, INFO, WARNING, ERROR
    }
}