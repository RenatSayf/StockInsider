package com.renatsayf.stockinsider.ui.result

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.DataTransferModel
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.AppLog
import com.renatsayf.stockinsider.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.android.synthetic.main.no_result_layout.*
import kotlinx.android.synthetic.main.no_result_layout.view.*
import kotlinx.android.synthetic.main.set_alert_layout.*
import javax.inject.Inject


@AndroidEntryPoint
class ResultFragment : Fragment()
{
    companion object
    {
        val TAG : String = this::class.java.simpleName
    }
    private lateinit var viewModel : ResultViewModel
    private lateinit var dataTransferModel : DataTransferModel

    @Inject
    lateinit var searchRequest: SearchRequest

    @Inject
    lateinit var confirmationDialog : ConfirmationDialog

    @Inject
    lateinit var notification : ServiceNotification

    @Inject
    lateinit var appCalendar: AppCalendar

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var appLog: AppLog

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

        viewModel.addAlarmVisibility.observe(viewLifecycleOwner, {
            it?.let{ addAlarmImgView.visibility = it }
        })

        viewModel.alarmOnVisibility.observe(viewLifecycleOwner, {
            it?.let{ alarmOnImgView.visibility = it }
        })

        dataTransferModel.getDealList().observe(viewLifecycleOwner, {
            it.let { list ->
                when
                {
                    list.size > 0 && list[0].error!!.isEmpty()     ->
                    {
                        resultTV.text = list.size.toString()
                        val linearLayoutManager = LinearLayoutManager(activity)
                        val dealListAdapter = DealListAdapter(list)
                        dealListAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                        tradeListRV.apply {
                            setHasFixedSize(true)
                            layoutManager = linearLayoutManager
                            adapter = dealListAdapter
                        }
                        return@let
                    }
                    list.size == 1 && list[0].error!!.isNotEmpty() ->
                    {
                        resultTV.text = 0.toString()
                        recommendationsTV.text = requireContext().getString(R.string.text_data_not_avalible)
                        noResultLayout.visibility = View.VISIBLE
                    }
                    else                                           ->
                    {
                        resultTV.text = list.size.toString()
                        noResultLayout.visibility = View.VISIBLE
                    }
                }
            }
        })

        addAlarmImgView.setOnClickListener {
            confirmationDialog.message = getString(R.string.text_confirm_search)
            confirmationDialog.flag = ""
            confirmationDialog.show(parentFragmentManager, ConfirmationDialog.TAG)
        }

        confirmationDialog.eventOk.observe(viewLifecycleOwner, { event ->
            if (!event.hasBeenHandled)
            {
                event.getContent().let { flag ->
                    when
                    {
                        flag != ConfirmationDialog.FLAG_CANCEL ->
                        {
                            context?.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)?.edit {
                                putBoolean(AlarmPendingIntent.IS_ALARM_SETUP_KEY, true)
                                apply()
                            }
                            viewModel.setAddAlarmVisibility(View.GONE)
                            viewModel.setAlarmOnVisibility(View.VISIBLE)
                            context?.getString(R.string.text_searching_is_created)?.let { msg ->
                                Snackbar.make(alarmOnImgView, msg, Snackbar.LENGTH_LONG).show()
                            }
                        }
                        else ->
                        {
                            activity?.let{ a ->
                                (a as MainActivity).setAlarmSetting(false)
                                a.getString(R.string.text_search_is_disabled).let { msg ->
                                    viewModel.setAddAlarmVisibility(View.VISIBLE)
                                    viewModel.setAlarmOnVisibility(View.GONE)
                                    Snackbar.make(tradeListRV, msg, Snackbar.LENGTH_LONG).show()
                                }
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

        activity?.let{ a ->
            when ((a as MainActivity).isServiceRunning())
            {
                true ->
                {
                    viewModel.setAddAlarmVisibility(View.GONE)
                    viewModel.setAlarmOnVisibility(View.VISIBLE)
                }
            }
        }

        StockInsiderService.serviceEvent.observe(viewLifecycleOwner, { event ->
            if (!event.hasBeenHandled)
            {
                when (event.getContent())
                {
                    StockInsiderService.STOP_KEY ->
                    {
                        context?.getString(R.string.text_search_is_disabled)?.let { msg ->
                            viewModel.setAddAlarmVisibility(View.VISIBLE)
                            viewModel.setAlarmOnVisibility(View.GONE)
                            Snackbar.make(tradeListRV, msg, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })


    }


}
