package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.renatsayf.stockinsider.utils.Event

class ConfirmationDialog constructor(private val message : String) : DialogFragment()
{
    companion object{
        const val TAG = "confirmation_dialog"
    }
    val eventOk : MutableLiveData<Event<Unit>> = MutableLiveData()

    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog
    {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
            .setPositiveButton("Ok") { _, _ ->
                eventOk.value = Event(Unit)
            }
            .setNegativeButton("Cancel"){ _, _ ->
                dismiss()
            }

        return builder.create()
    }
}