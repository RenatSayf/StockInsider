package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import kotlinx.android.synthetic.main.save_search_layout.*
import kotlinx.android.synthetic.main.save_search_layout.view.*

class SaveSearchDialog : DialogFragment()
{

    companion object
    {
        val TAG = this::class.java.canonicalName.plus("_tag")
        private val KEY_SEARCH_NAME = this::class.java.canonicalName.plus("_key_search_name")
        private var instance: SaveSearchDialog? = null
        fun getInstance(searchName: String = ""): SaveSearchDialog = if (instance == null)
        {
            SaveSearchDialog().apply {
                arguments = Bundle().apply {
                    putString(KEY_SEARCH_NAME, searchName)
                }
            }
        }
        else instance as SaveSearchDialog

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.save_search_layout, LinearLayout(requireContext()), false)

        dialogView.searchNameView.setText(arguments?.getString(KEY_SEARCH_NAME).toString())

        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.text_saving_search))
            setView(dialogView)
            setPositiveButton(getString(R.string.text_save), object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    dismiss()
                }

            })
            setNegativeButton(getString(R.string.text_cancel), object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    dismiss()
                }

            })
        }
        return builder.create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

    }


}