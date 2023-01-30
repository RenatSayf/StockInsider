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
        const val KEY_NOT_SHOW_AGAN = "KEY_NOT_SHOW_AGAN5555222"

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

    private var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return AlertDialog.Builder(requireContext()).apply {
            when(status) {
                DialogStatus.SUCCESS -> setIcon(R.drawable.ic_circle_check_green)
                DialogStatus.INFO -> setIcon(R.drawable.ic_info_blue)
                DialogStatus.ERROR -> setIcon(R.drawable.ic_error_red)
                DialogStatus.WARNING -> setIcon(R.drawable.ic_warning_yellow)
                DialogStatus.EXTENDED_WARNING -> setIcon(R.drawable.ic_warning_yellow)
            }
            if (title.isNotEmpty()) {
                setTitle(title)
            }
            if (message.isNotEmpty()) {
                setMessage(message)
            }
            setPositiveButton(getString(R.string.text_ok), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    listener?.onInfoDialogButtonClick(1)
                    dismiss()
                }
            })
            if (status == DialogStatus.EXTENDED_WARNING) {
                setNeutralButton(getString(R.string.text_not_show_again), object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        listener?.onInfoDialogButtonClick(0)
                    }
                })
                setNegativeButton(getString(R.string.text_cancel), object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dismiss()
                    }
                })
            }
        }.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
        }
    }

    fun setOnPositiveClickListener(listener: Listener) {
        this.listener = listener
    }

    enum class DialogStatus {
        SUCCESS, INFO, WARNING, ERROR, EXTENDED_WARNING
    }

    interface Listener {
        fun onInfoDialogButtonClick(result: Int = 1)
    }
}