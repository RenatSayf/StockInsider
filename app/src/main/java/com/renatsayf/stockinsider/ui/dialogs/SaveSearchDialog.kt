package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import kotlinx.android.synthetic.main.save_search_layout.view.*

class SaveSearchDialog : DialogFragment()
{
    private lateinit var listener: OnClickListener

    companion object
    {
        val TAG = this::class.java.canonicalName.plus("_tag")
        val KEY_SEARCH_NAME = this::class.java.canonicalName.plus("_key_search_name")
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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        listener = ViewModelProvider(activity as MainActivity)[OnClickListener::class.java]
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
                    if (!dialogView.searchNameView.text.isNullOrEmpty())
                    {
                        val name = dialogView.searchNameView.text.toString()
                        listener.pushData(Pair(KEY_SEARCH_NAME, name))
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

    class OnClickListener : ViewModel()
    {
        private val _click = MutableLiveData<Pair<String?, String>>()
        fun pushData(data: Pair<String?, String>)
        {
            _click.value = data
        }
        var onClick: LiveData<Pair<String?, String>> = _click
    }


}