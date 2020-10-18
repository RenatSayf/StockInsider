package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchListDialog : DialogFragment()
{
    private lateinit var mainViewModel: MainViewModel
    private lateinit var listener: OnClickListener

    companion object
    {
        val TAG = this::class.java.canonicalName.plus("_tag")
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        listener = ViewModelProvider(requireActivity())[OnClickListener::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val searchIdList = mutableListOf<String>()
        val allSearchSets = mainViewModel.getAllSearchSets()
        allSearchSets.forEach{
            searchIdList.add(it.setName)
        }

        var index : Int = -1
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle("Мои поисковые запросы")
            setSingleChoiceItems(searchIdList.toTypedArray(), -1, object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    index = p1
                    //println("*********************** SingleChoiceItems index = $index *****************************")
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
                        listener.pushData(roomSearchSet)
                    }
                }
            })
        }
        return builder.create()
    }

    class OnClickListener : ViewModel()
    {
        private val _click = MutableLiveData<RoomSearchSet>()
        fun pushData(data: RoomSearchSet)
        {
            _click.value = data
        }
        var onClick: LiveData<RoomSearchSet> = _click
    }
}