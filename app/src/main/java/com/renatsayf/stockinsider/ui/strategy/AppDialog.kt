@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.strategy

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.AppDialogLayoutBinding
import com.renatsayf.stockinsider.utils.Event

class AppDialog : DialogFragment()
{
    private lateinit var binding: AppDialogLayoutBinding
    private lateinit var observer: EventObserver

    companion object
    {
        val TAG = this::class.java.simpleName.plus("_tag")
        private lateinit var message: SpannableStringBuilder
        private lateinit var positiveText: String
        private var negativeText: String? = null
        private var neutralText: String? = null
        private var dialogTag: String = ""

        fun getInstance(tag: String,
                        message: SpannableStringBuilder,
                        positiveText: String = "OK",
                        negativeText: String? = "Cancel",
                        neutralText: String? = null): AppDialog = run {
            this.dialogTag = tag
            this.message = message
            this.positiveText = positiveText
            this.negativeText = negativeText
            this.neutralText = neutralText
            AppDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        observer = ViewModelProvider(activity as MainActivity)[EventObserver::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        binding = AppDialogLayoutBinding.inflate(layoutInflater)
        binding.dialogTextView.text = message
        val builder = AlertDialog.Builder(requireContext()).apply {
            setView(binding.root)
            setPositiveButton(positiveText, object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    val pair = Pair(dialogTag, p1)
                    observer.data.value = Event(pair)
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
                    observer.data.value = Event(Pair(dialogTag, p1))
                    dismiss()
                }
            })
        }
        return builder.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
        }
    }



    class EventObserver : ViewModel()
    {
        val data = MutableLiveData<Event<Pair<String?, Int>>>()
    }
}