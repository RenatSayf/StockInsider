package com.renatsayf.stockinsider.ui.strategy

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.utils.Event
import kotlinx.android.synthetic.main.app_dialog_layout.view.*

class AppDialog : DialogFragment()
{
    private lateinit var listener: EventListener

    companion object
    {
        val TAG = this::class.java.canonicalName.plus("_tag")
        private lateinit var message: SpannableStringBuilder
        private lateinit var positiveText: String
        private var negativeText: String? = null
        private var neutralText: String? = null
        private var dialogTag: String = ""
        private var instance: AppDialog? = null
        fun getInstance(tag: String,
                        message: SpannableStringBuilder,
                        positiveText: String = "OK",
                        negativeText: String? = "Cancel",
                        neutralText: String? = null): AppDialog = if (instance == null)
        {
            this.dialogTag = tag
            this.message = message
            this.positiveText = positiveText
            this.negativeText = negativeText
            this.neutralText = neutralText
            AppDialog()
        }
        else instance as AppDialog
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        listener = ViewModelProvider(activity as MainActivity)[EventListener::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.app_dialog_layout, LinearLayout(requireContext()), false)
        dialogView.dialogTextView.text = message
        val builder = AlertDialog.Builder(requireContext()).apply {
            setView(dialogView)
            setPositiveButton(positiveText, object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    val pair = Pair(dialogTag, p1)
                    listener.data.value = Event(pair)
                }
            })
        }
        negativeText?.let {
            builder.setNegativeButton(it, object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    dismiss()
                }

            })
        }
        neutralText?.let {
            builder.setNeutralButton(it, object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    listener.data.value = Event(Pair(dialogTag, p1))
                    dismiss()
                }
            })
        }
        return builder.create()
    }



    class EventListener : ViewModel()
    {
        val data = MutableLiveData<Event<Pair<String?, Int>>>()
    }
}