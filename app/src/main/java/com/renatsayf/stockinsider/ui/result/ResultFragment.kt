package com.renatsayf.stockinsider.ui.result

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
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
import com.renatsayf.stockinsider.network.Scheduler
import com.renatsayf.stockinsider.receivers.AlarmReceiver
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.utils.IsFilingTime
import com.renatsayf.stockinsider.utils.Utils
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.android.synthetic.main.no_result_layout.*
import kotlinx.android.synthetic.main.no_result_layout.view.*
import kotlinx.android.synthetic.main.set_alert_layout.*
import java.util.*
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

        val existsStartPending: PendingIntent? = context?.let {
            createPendingIntent(it, AlarmReceiver.ACTION_START_ALARM, startCode, PendingIntent.FLAG_NO_CREATE)
        }
        val existsEndPending: PendingIntent? = context?.let {
            createPendingIntent(it, AlarmReceiver.ACTION_END_ALARM, endCode, PendingIntent.FLAG_NO_CREATE)
        }
        when (existsEndPending == null && existsStartPending == null)
        {
            true ->
            {
                println("existsStartPending == $existsStartPending ********************************************************")
                println("existsEndPending == $existsEndPending ********************************************************")
                addAlarmImgView.visibility = View.VISIBLE
                alarmOnImgView.visibility = View.GONE
            }
            else ->
            {
                println("existsStartPending == $existsStartPending ********************************************************")
                println("existsEndPending == $existsEndPending ********************************************************")
                addAlarmImgView.visibility = View.GONE
                alarmOnImgView.visibility = View.VISIBLE
            }
        }

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
                                alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                startIntent = createPendingIntent(context, AlarmReceiver.ACTION_START_ALARM, startCode, PendingIntent.FLAG_CANCEL_CURRENT)
                                println("Создан startIntent == $startIntent ********************************************************")

                                val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
                                val startCalendar = Calendar.getInstance(timeZone).apply {
                                    timeInMillis = System.currentTimeMillis()
                                    set(Calendar.HOUR_OF_DAY, IsFilingTime.START_HOUR)
                                    set(Calendar.MINUTE, IsFilingTime.START_MINUTE)
                                }
                                alarmManager?.apply {
                                    setRepeating(AlarmManager.RTC_WAKEUP, startCalendar.timeInMillis, IsFilingTime.INTERVAL, startIntent)
                                }
                                val startTime = utils.getFormattedDateTime(0, Date(startCalendar.timeInMillis))

                                val (isFilingTime, isAfterFiling) = IsFilingTime.checking(timeZone)
                                val message = when(isFilingTime)
                                {
                                    true -> "The schedule is set. We will check new deals every hour"
                                    false -> "The schedule is set at $startTime"
                                }
                                showMessage(message)

                                endIntent = createPendingIntent(context, AlarmReceiver.ACTION_END_ALARM, endCode, PendingIntent.FLAG_CANCEL_CURRENT)
                                println("Создан endIntent == $endIntent ********************************************************")
                                val endCalendar = Calendar.getInstance(timeZone).apply {
                                    set(Calendar.HOUR_OF_DAY, IsFilingTime.END_HOUR)
                                    set(Calendar.MINUTE, IsFilingTime.END_MINUTE)
                                }
                                alarmManager?.setRepeating(AlarmManager.RTC_WAKEUP, endCalendar.timeInMillis, IsFilingTime.INTERVAL, endIntent)

                                addAlarmImgView.visibility = View.GONE
                                alarmOnImgView.visibility = View.VISIBLE
                            }
                        }
                        else ->
                        {
                            context?.let { context ->
                                createPendingIntent(context, AlarmReceiver.ACTION_START_ALARM, startCode, PendingIntent.FLAG_CANCEL_CURRENT).also { startPending ->
                                        alarmManager?.cancel(startPending)
                                        startPending?.cancel()
                                    }
                                createPendingIntent(context, AlarmReceiver.ACTION_END_ALARM, endCode, PendingIntent.FLAG_CANCEL_CURRENT).also { endPending ->
                                    alarmManager?.cancel(endPending)
                                    endPending?.cancel()
                                }

//                                endIntent = Intent(context, AlarmReceiver::class.java).let {endIntent ->
//                                    endIntent.action = AlarmReceiver.ACTION_END_ALARM
//                                    val endPending = PendingIntent.getBroadcast(
//                                        context,
//                                        endCode,
//                                        endIntent,
//                                        PendingIntent.FLAG_CANCEL_CURRENT
//                                    )
//                                    endPending.also {
//                                        alarmManager?.cancel(it)
//                                        it.cancel()
//                                    }
//                                }

                                addAlarmImgView.visibility = View.VISIBLE
                                alarmOnImgView.visibility = View.GONE
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

        when((activity as MainActivity).isServiceRunning())
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

    private fun createPendingIntent(context: Context, action: String, code: Int, flag: Int) : PendingIntent?
    {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = action
            PendingIntent.getBroadcast(context, code, intent, flag)
        }
    }

}
