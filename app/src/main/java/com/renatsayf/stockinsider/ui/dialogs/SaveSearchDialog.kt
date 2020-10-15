package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R

class SaveSearchDialog : DialogFragment()
{

    companion object
    {
        val TAG = this::class.java.canonicalName.plus("_tag")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.save_search_layout, LinearLayout(requireContext()), false)
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle("Title")
            setView(dialogView)
            setPositiveButton("Сохранить", object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    dismiss()
                }

            })
            setNegativeButton("Отмена", object : DialogInterface.OnClickListener
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