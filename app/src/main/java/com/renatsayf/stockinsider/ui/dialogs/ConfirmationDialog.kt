package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


class ConfirmationDialog constructor(var message : String = "",
                                     private var btnOkText: String = "Ok",
                                     var flag : String = "") : DialogFragment()
{
    companion object{
        val TAG = this::class.java.simpleName.plus("_confirmation_dialog")
        val FLAG_CANCEL = this::class.java.simpleName.plus("flag_cancel")
    }

    private var listener: Listener? = null

    fun setOnClickListener(listener: Listener)
    {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        if (btnOkText.isEmpty()) btnOkText = getString(R.string.text_ok)
    }

    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog
    {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
            .setPositiveButton(btnOkText) { _: DialogInterface?, _: Int ->
                if (flag != FLAG_CANCEL)
                {
                    listener?.onPositiveClick("")
                }
                else
                {
                    listener?.onPositiveClick(FLAG_CANCEL)
                }

            }
            .setNegativeButton(getString(R.string.text_cancel)){ _, _ ->
                dismiss()
            }

        return builder.create()
    }

    interface Listener
    {
        fun onPositiveClick(flag: String)
    }
}