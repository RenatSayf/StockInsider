package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
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
    private lateinit var mainViewModel: MainViewModel
    private lateinit var observer: EventObserver //TODO Sending events between Activities/Fragments: Step 3

    companion object
    {
        val TAG = this::class.java.canonicalName.plus("_tag")
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        observer = ViewModelProvider(requireActivity())[EventObserver::class.java] //TODO Sending events between Activities/Fragments: Step 4
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        CoroutineScope(Dispatchers.Main).launch {
            val allSearchSets = mainViewModel.getUserSearchSets() as ArrayList<RoomSearchSet>
            allSearchSets.removeIf { s ->
                s.queryName == getString(R.string.text_default_set_name) || s.queryName == getString(
                        R.string.text_current_set_name
                )
            }

            val searchIdList = mutableListOf<String>()
            allSearchSets.forEach {
                searchIdList.add(it.queryName)
            }

            var index: Int = -1
            AlertDialog.Builder(requireContext()).apply {
                setTitle(getString(R.string.text_my_search_requests))
                setSingleChoiceItems(
                        searchIdList.toTypedArray(),
                        -1,
                        object : DialogInterface.OnClickListener
                        {
                            override fun onClick(p0: DialogInterface?, p1: Int)
                            {
                                index = p1
                            }
                        })
                setPositiveButton(
                        getString(R.string.text_ok),
                        object : DialogInterface.OnClickListener
                        {
                            override fun onClick(p0: DialogInterface?, p1: Int)
                            {
                                //println("*********************** PositiveButton index = $index *****************************")
                                if (index > -1)
                                {
                                    val roomSearchSet = allSearchSets[index]
                                    //TODO Sending events between Activities/Fragments: Step 5
                                    // next step in com.renatsayf.stockinsider.ui.dialogs.MainFragment.kt
                                    observer.data.value = Event(roomSearchSet)
                                }
                                dismiss()
                            }
                        })
                setNegativeButton(
                        getString(R.string.text_delete),
                        object : DialogInterface.OnClickListener
                        {
                            override fun onClick(p0: DialogInterface?, p1: Int)
                            {
                                if (index > -1)
                                {
                                    val roomSearchSet = allSearchSets[index]
                                    CoroutineScope(Dispatchers.Main).launch {
                                        mainViewModel.deleteSearchSet(roomSearchSet)

                                    }
                                }
                                dismiss()
                            }

                        })
                setOnCancelListener { dismiss() }
                create()
                show()
            }
        }
        return super.onCreateDialog(savedInstanceState)
    }



    //TODO Sending events between Activities/Fragments: Step 2 - create this class,
    // next step in com.renatsayf.stockinsider.ui.dialogs.SearchListDialog.kt
    class EventObserver : ViewModel()
    {
        val data = MutableLiveData<Event<RoomSearchSet>>()
    }
}