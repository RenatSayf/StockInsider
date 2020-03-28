package com.renatsayf.stockinsider.ui.result

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
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
import com.renatsayf.stockinsider.network.Scheduler
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.utils.Utils
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.android.synthetic.main.no_result_layout.*
import kotlinx.android.synthetic.main.no_result_layout.view.*
import kotlinx.android.synthetic.main.set_alert_layout.*
import javax.inject.Inject


class ResultFragment : Fragment(), StockInsiderService.IShowMessage
{
    private lateinit var viewModel : ResultViewModel
    private lateinit var dataTransferModel : DataTransferModel

    @Inject
    lateinit var scheduler : Scheduler

    @Inject
    lateinit var confirmationDialog : ConfirmationDialog

    @Inject
    lateinit var notification : ServiceNotification

    @Inject
    lateinit var utils: Utils

    private var alarmManager: AlarmManager? = null
    private var startIntent: PendingIntent? = null
    private val startCode: Int = 7
    private var endIntent: PendingIntent? = null
    private val endCode: Int = 21

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        MainActivity.appComponent.inject(this)
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

        addAlarmImgView.setOnClickListener {
            confirmationDialog.message = getString(R.string.text_confirm_search)
            confirmationDialog.flag = ""
            confirmationDialog.show(parentFragmentManager, ConfirmationDialog.TAG)
        }

        confirmationDialog.eventOk.observe(viewLifecycleOwner, Observer { it ->
            if (!it.hasBeenHandled)
            {
                it.getContent().let {flag ->
                    when
                    {
                        flag != ConfirmationDialog.FLAG_CANCEL ->
                        {
                            context?.let { context ->
                                addAlarmImgView.visibility = View.GONE
                                alarmOnImgView.visibility = View.VISIBLE
                                with(activity?.getPreferences(Context.MODE_PRIVATE)?.edit())
                                {
                                    this?.putBoolean(StockInsiderService.PREFERENCE_KEY, true)
                                    this?.apply()
                                }
                                Snackbar.make(alarmOnImgView, context.getString(R.string.text_searching_is_created), Snackbar.LENGTH_LONG).show()
                            }
                        }
                        else ->
                        {
                            context?.let { context ->
                                addAlarmImgView.visibility = View.VISIBLE
                                alarmOnImgView.visibility = View.GONE
                                with(activity?.getPreferences(Context.MODE_PRIVATE)?.edit())
                                {
                                    this?.putBoolean(StockInsiderService.PREFERENCE_KEY, false)
                                    this?.apply()
                                }
                                Snackbar.make(addAlarmImgView, context.getString(R.string.text_search_is_disabled), Snackbar.LENGTH_LONG).show()
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

        when((activity as MainActivity).getPreferences(Context.MODE_PRIVATE).getBoolean(StockInsiderService.PREFERENCE_KEY, false))
        {
            true ->
            {
                addAlarmImgView.visibility = View.GONE
                alarmOnImgView.visibility = View.VISIBLE
            }
        }

        return
    }

    override fun showMessage(text : String)
    {
        Snackbar.make(addAlarmImgView, text, Snackbar.LENGTH_LONG).show()
    }


}
