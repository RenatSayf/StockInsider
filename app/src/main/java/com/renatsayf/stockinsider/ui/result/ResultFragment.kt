package com.renatsayf.stockinsider.ui.result

import android.app.PendingIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.DataTransferModel
import com.renatsayf.stockinsider.network.ScheduleReceiver
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.android.synthetic.main.no_result_layout.*
import kotlinx.android.synthetic.main.no_result_layout.view.*
import kotlinx.android.synthetic.main.set_alert_layout.*

class ResultFragment : Fragment()
{
    private lateinit var viewModel : ResultViewModel
    private lateinit var dataTransferModel : DataTransferModel

    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View?
    {
        val root = inflater.inflate(R.layout.fragment_result, container, false)
        root.noResultLayout.visibility = View.GONE
        return root
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ResultViewModel::class.java)
        dataTransferModel = activity?.run {
            ViewModelProvider(activity as MainActivity)[DataTransferModel::class.java]
        }!!

        dataTransferModel.getDealList().observe(viewLifecycleOwner, Observer {
            it.let {
                if (it.size > 0)
                {
                    resultTV.text = it.size.toString()
                    val linearLayoutManager = LinearLayoutManager(activity)
                    val dealListAdapter = DealListAdapter(activity as MainActivity, it)
                    tradeListRV.apply {
                        setHasFixedSize(true)
                        layoutManager = linearLayoutManager
                        adapter = dealListAdapter
                    }
                    return@let
                }
                else
                {
                    resultTV.text = it.size.toString()
                    noResultLayout.visibility = View.VISIBLE
                }
            }
        })

        val confirmationDialog = ConfirmationDialog(getString(R.string.text_confirm_search))
        addAlertImgView.setOnClickListener {
            confirmationDialog.show(parentFragmentManager, ConfirmationDialog.TAG)
        }

        confirmationDialog.eventOk.observe(viewLifecycleOwner, Observer {
            if (!it.hasBeenHandled)
            {
                it.getContent().let {
                    val scheduleReceiver = ScheduleReceiver()
                    val intent = context?.let { context -> scheduleReceiver.setAlarm(context) }
                    if (intent is PendingIntent)
                    {
                        addAlertImgView.visibility = View.GONE
                        alarmOnImgView.visibility = View.VISIBLE
                        Snackbar.make(addAlertImgView, getString(R.string.text_searching_is_created), Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        })

        alarmOnImgView.setOnClickListener {

        }

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }
    }


}
