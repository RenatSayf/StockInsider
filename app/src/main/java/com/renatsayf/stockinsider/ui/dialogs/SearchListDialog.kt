@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.RoomSearchSet
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchListDialog : DialogFragment()
{
    companion object
    {
        val TAG = this::class.java.simpleName.plus("_tag")
        private lateinit var list: MutableList<RoomSearchSet>
        private lateinit var listener: Listener

        fun newInstance(list: MutableList<RoomSearchSet>, listener: Listener): SearchListDialog {
            this.list = list
            this.listener = listener
            return SearchListDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        list.removeIf { s -> s.queryName == getString(R.string.text_current_set_name) }

        val searchIdList = mutableListOf<String>()
        list.forEach {
            searchIdList.add(it.queryName)
        }

        var index: Int = -1
        return AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.text_my_search_requests))
            if (list.isEmpty()) setMessage(getString(R.string.text_no_search_request))
            setSingleChoiceItems(searchIdList.toTypedArray(),
                -1,
                object : DialogInterface.OnClickListener
                {
                    override fun onClick(p0: DialogInterface?, p1: Int)
                    {
                        index = p1
                    }
                })
            setPositiveButton(getString(R.string.text_ok), object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    if (index > -1)
                    {
                        val roomSearchSet = list[index]
                        listener.onSearchDialogPositiveClick(roomSearchSet)
                    }
                    dismiss()
                }
            })
            if (list.isNotEmpty())
            {
                setNegativeButton(getString(R.string.text_delete),
                    object : DialogInterface.OnClickListener
                    {
                        override fun onClick(p0: DialogInterface?, p1: Int)
                        {
                            if (index > -1)
                            {
                                val roomSearchSet = list[index]
                                listener.onSearchDialogDeleteClick(roomSearchSet)
                            }
                            dismiss()
                        }
                    })
            }
            setOnCancelListener { dismiss() }
        }.create()
    }

    interface Listener {
        fun onSearchDialogPositiveClick(roomSearchSet: RoomSearchSet)
        fun onSearchDialogDeleteClick(roomSearchSet: RoomSearchSet)
    }
}