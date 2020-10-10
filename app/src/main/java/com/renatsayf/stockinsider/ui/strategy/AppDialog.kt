package com.renatsayf.stockinsider.ui.strategy

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import kotlinx.android.synthetic.main.app_dialog_layout.view.*

class AppDialog : DialogFragment()
{


    companion object
    {
        val TAG = this::class.java.canonicalName.plus("_tag")
        private lateinit var message: SpannableStringBuilder
        private lateinit var btnOkText: String
        private var instance: AppDialog? = null
        fun getInstance(message: SpannableStringBuilder, btnOkText: String = "OK"): AppDialog = if (instance == null)
        {
            this.message = message
            this.btnOkText = btnOkText
            AppDialog()
        }
        else instance as AppDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.app_dialog_layout, LinearLayout(requireContext()), false)
        dialogView.dialogTextView.text = message
        val builder = AlertDialog.Builder(requireContext()).apply {
            setView(dialogView)
            setPositiveButton(btnOkText, object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    (requireActivity() as MainActivity).navController.navigate(R.id.nav_strategy)
                }
            })
            setNegativeButton("Закрыть", object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    dismiss()
                }

            })
        }
        return builder.create()
    }
}