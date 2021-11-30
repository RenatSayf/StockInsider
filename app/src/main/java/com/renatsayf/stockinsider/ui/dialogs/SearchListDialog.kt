@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.*
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.utils.Event
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private lateinit var observer: EventObserver //TODO Sending events between Activities/Fragments: Step 3

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        observer = ViewModelProvider(requireActivity())[EventObserver::class.java] //TODO Sending events between Activities/Fragments: Step 4

        mainViewModel.get_UserSearchSets()
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
            if (list.isEmpty()) setMessage("Нет сохраненных поисковых запросов")
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
                        //TODO Sending events between Activities/Fragments: Step 5
                        // next step in com.renatsayf.stockinsider.ui.dialogs.MainFragment.kt
                        observer.data.value = Event(roomSearchSet)
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



    //TODO Sending events between Activities/Fragments: Step 2 - create this class,
    // next step in com.renatsayf.stockinsider.ui.dialogs.SearchListDialog.kt
    class EventObserver : ViewModel()
    {
        val data = MutableLiveData<Event<RoomSearchSet>>()
    }

    interface Listener {
        fun onSearchDialogDeleteClick(roomSearchSet: RoomSearchSet)
    }
}