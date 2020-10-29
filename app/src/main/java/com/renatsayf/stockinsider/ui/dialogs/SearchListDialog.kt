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


@AndroidEntryPoint
class SearchListDialog : DialogFragment()
{
    private lateinit var mainViewModel: MainViewModel
    private lateinit var listener: EventListener //TODO Sending events between Activities/Fragments: Step 3

    companion object
    {
        val TAG = this::class.java.canonicalName.plus("_tag")
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        listener = ViewModelProvider(requireActivity())[EventListener::class.java] //TODO Sending events between Activities/Fragments: Step 4
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val allSearchSets = mainViewModel.getAllSearchSets() as ArrayList<RoomSearchSet>
        allSearchSets.removeIf {s -> s.queryName == getString(R.string.text_default_set_name) || s.queryName == getString(R.string.text_current_set_name)}

        val searchIdList = mutableListOf<String>()
        allSearchSets.forEach{
            searchIdList.add(it.queryName)
        }

        var index : Int = -1
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.text_my_search_requests))
            setSingleChoiceItems(searchIdList.toTypedArray(), -1, object : DialogInterface.OnClickListener
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
                    //println("*********************** PositiveButton index = $index *****************************")
                    if (index > -1)
                    {
                        val roomSearchSet = allSearchSets[index]
                        //TODO Sending events between Activities/Fragments: Step 5
                        // next step in com.renatsayf.stockinsider.ui.dialogs.MainFragment.kt
                        listener.data.value = Event(roomSearchSet)
                    }
                }
            })
        }
        return builder.create()
    }



    //TODO Sending events between Activities/Fragments: Step 2 - create this class,
    // next step in com.renatsayf.stockinsider.ui.dialogs.SearchListDialog.kt
    class EventListener : ViewModel()
    {
        val data = MutableLiveData<Event<RoomSearchSet>>()
    }
}