@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.SaveSearchLayoutBinding

class SaveSearchDialog private constructor(): DialogFragment()
{
    private lateinit var binding: SaveSearchLayoutBinding

    companion object
    {
        val TAG = this::class.java.simpleName.plus("_tag")
        private var listener: Listener? = null
        private var name: String? = null

        fun getInstance(name: String, listener: Listener? = null): SaveSearchDialog
        {
            this.name = name
            this.listener = listener
            return SaveSearchDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        binding = SaveSearchLayoutBinding.inflate(requireActivity().layoutInflater)

        binding.searchNameView.setText(name)

        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.text_saving_search))
            setView(binding.root)
            setPositiveButton(getString(R.string.text_save), object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    if (!binding.searchNameView.text.isNullOrEmpty())
                    {
                        val name = binding.searchNameView.text.toString()
                        listener?.onSaveSearchDialogPositiveClick(name)
                        dismiss()
                    }
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

    interface Listener {
        fun onSaveSearchDialogPositiveClick(searchName: String)
    }


}