package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.utils.Event
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmationDialog constructor(var message : String, var btnOkText: String = "Ok", var flag : String) : DialogFragment()
{
    companion object{
        const val TAG = "confirmation_dialog"
        const val FLAG_CANCEL = "cancel"
    }
    val eventOk : MutableLiveData<Event<String>> = MutableLiveData()

    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog
    {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
            .setPositiveButton(btnOkText) { _, _ ->
                if (flag != FLAG_CANCEL)
                {
                    eventOk.value = Event("")
                }
                else
                {
                    eventOk.value = Event(FLAG_CANCEL)
                }
            }
            .setNegativeButton(getString(R.string.text_cancel)){ _, _ ->
                dismiss()
            }

        return builder.create()
    }
}