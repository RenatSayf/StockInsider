package com.renatsayf.stockinsider.ui.result

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
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.models.DataTransferModel
import com.renatsayf.stockinsider.network.ScheduleReceiver
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.android.synthetic.main.no_result_layout.*
import kotlinx.android.synthetic.main.no_result_layout.view.*
import kotlinx.android.synthetic.main.set_alert_layout.*
import javax.inject.Inject

class ResultFragment : Fragment()
{
    private lateinit var viewModel : ResultViewModel
    private lateinit var dataTransferModel : DataTransferModel

    @Inject
    lateinit var scheduleReceiver : ScheduleReceiver

    @Inject
    lateinit var confirmationDialog : ConfirmationDialog

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        App().component.inject(this)
    }

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
                when
                {
                    it.size > 0 && it[0].error!!.isEmpty() ->
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
                    it.size == 1 && it[0].error!!.isNotEmpty() ->
                    {
                        resultTV.text = 0.toString()
                        recommendationsTV.text = it[0].error
                        noResultLayout.visibility = View.VISIBLE
                    }
                    else ->
                    {
                        resultTV.text = it.size.toString()
                        noResultLayout.visibility = View.VISIBLE
                    }
                }
            }
        })

        addAlertImgView.setOnClickListener {
            confirmationDialog.message = getString(R.string.text_confirm_search)
            confirmationDialog.flag = ""
            confirmationDialog.show(parentFragmentManager, ConfirmationDialog.TAG)
        }

        confirmationDialog.eventOk.observe(viewLifecycleOwner, Observer {
            if (!it.hasBeenHandled)
            {
                it.getContent().let {flag ->
                    when
                    {
                        flag != ConfirmationDialog.FLAG_CANCEL ->
                        {
                            context?.let { context ->
                                scheduleReceiver.setNetSchedule(context)
                                //BootCompletedReceiver.enable(context)
                                addAlertImgView.visibility = View.GONE
                                alarmOnImgView.visibility = View.VISIBLE
                                Snackbar.make(addAlertImgView, getString(R.string.text_searching_is_created), Snackbar.LENGTH_LONG).show()
                            }
                        }
                        else ->
                        {
                            context?.let { it1 ->
                                scheduleReceiver.cancelNetSchedule(it1)
                                addAlertImgView.visibility = View.VISIBLE
                                alarmOnImgView.visibility = View.GONE
                                Snackbar.make(addAlertImgView, getString(R.string.text_search_is_disabled), Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        })

        alarmOnImgView.setOnClickListener {
            confirmationDialog.message = getString(R.string.text_cancel_search)
            confirmationDialog.flag = ConfirmationDialog.FLAG_CANCEL
            confirmationDialog.show(parentFragmentManager, ConfirmationDialog.TAG)
        }

        backButton.setOnClickListener {
            activity?.onBackPressed()
        }
    }


}
