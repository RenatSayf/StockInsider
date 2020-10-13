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
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.AppLog
import com.renatsayf.stockinsider.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_result.*
import kotlinx.android.synthetic.main.fragment_result.view.*
import kotlinx.android.synthetic.main.load_progress_layout.*
import kotlinx.android.synthetic.main.no_result_layout.*
import kotlinx.android.synthetic.main.no_result_layout.view.*
import kotlinx.android.synthetic.main.set_alert_layout.*
import javax.inject.Inject


@AndroidEntryPoint
class ResultFragment : Fragment()
{
    companion object
    {
        val TAG = this::class.java.canonicalName.toString().plus("_tag")
        val ARG_SET_NAME = this::class.java.canonicalName.toString().plus("_arg_set_name")
        val ARG_TITLE = this::class.java.canonicalName.toString().plus("_arg_title")
    }
    private lateinit var viewModel : ResultViewModel
    private lateinit var mainViewModel : MainViewModel
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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel = ViewModelProvider(this)[ResultViewModel::class.java]
        dataTransferModel = requireActivity().run {
            ViewModelProvider(this)[DataTransferModel::class.java]
        }
        if (savedInstanceState == null)
        {
            requireActivity().loadProgreesBar.visibility = View.VISIBLE
            val setName = arguments?.getString(ARG_SET_NAME)

            if (!setName.isNullOrEmpty())
            {
                val mainActivity = activity as MainActivity
                val set = mainViewModel.getSearchSet(setName)
                val searchSet = SearchSet(set.setName).apply {
                    ticker = mainActivity.getTickersString(set.ticker)
                    filingPeriod = mainActivity.getFilingOrTradeValue(set.filingPeriod)
                    tradePeriod = mainActivity.getFilingOrTradeValue(set.tradePeriod)
                    isPurchase = mainActivity.getCheckBoxValue(set.isPurchase)
                    isSale = mainActivity.getCheckBoxValue(set.isSale)
                    tradedMin = set.tradedMin
                    tradedMax = set.tradedMax
                    isOfficer = mainActivity.getOfficerValue(set.isOfficer)
                    isDirector = mainActivity.getCheckBoxValue(set.isDirector)
                    isTenPercent = mainActivity.getCheckBoxValue(set.isTenPercent)
                    groupBy = mainActivity.getGroupingValue(set.groupBy)
                    sortBy = mainActivity.getSortingValue(set.sortBy)
                }
                mainViewModel.getDealList(searchSet)
            }
        }
    }

    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View?
    {
        val root = inflater.inflate(R.layout.fragment_result, container, false)
        root.noResultLayout.visibility = View.GONE

        //region Показываем фейковый список пока подгружаются данные
        val fakeList = ArrayList<Deal>()
        for (i in 0..10) fakeList.add(Deal("xxxxxxxx"))
        val linearLayoutManager = LinearLayoutManager(activity)
        val dealListAdapter = DealListAdapter(fakeList, R.layout.fake_deal_layout)
        root.tradeListRV.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = dealListAdapter
        }
        //endregion
        return root
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        viewModel.addAlarmVisibility.observe(viewLifecycleOwner, {
            it?.let{ addAlarmImgView.visibility = it }
        })

        viewModel.alarmOnVisibility.observe(viewLifecycleOwner, {
            it?.let{ alarmOnImgView.visibility = it }
        })

        viewModel.toolBarTitle.observe(viewLifecycleOwner, {
            requireActivity().toolbar.title = it
        })

        val title = arguments?.getString(ARG_TITLE) ?: ""
        viewModel.setToolBarTitle(title)

        mainViewModel.dealList.observe(viewLifecycleOwner, {
            it.let { list ->
                requireActivity().loadProgreesBar?.visibility = View.GONE
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

        mainViewModel.documentError.observe(viewLifecycleOwner, {
            requireActivity().loadProgreesBar?.visibility = View.GONE
            when (it) {
                is IndexOutOfBoundsException ->
                {
                    val dealList: ArrayList<Deal> = arrayListOf()
                    when
                    {
                        dealList.size > 0 && dealList[0].error!!.isEmpty()     ->
                        {
                            resultTV.text = dealList.size.toString()
                            val linearLayoutManager = LinearLayoutManager(activity)
                            val dealListAdapter = DealListAdapter(dealList)
                            dealListAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                            tradeListRV.apply {
                                setHasFixedSize(true)
                                layoutManager = linearLayoutManager
                                adapter = dealListAdapter
                            }
                        }
                        dealList.size == 1 && dealList[0].error!!.isNotEmpty() ->
                        {
                            resultTV.text = 0.toString()
                            recommendationsTV.text = requireContext().getString(R.string.text_data_not_avalible)
                            noResultLayout.visibility = View.VISIBLE
                        }
                        else                                                   ->
                        {
                            resultTV.text = dealList.size.toString()
                            noResultLayout.visibility = View.VISIBLE
                        }
                    }
                }
                else ->
                {
                    if (it != null)
                    {
                        Snackbar.make(tradeListRV, it.message.toString(), Snackbar.LENGTH_LONG).show()
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

    override fun onDestroy()
    {
        super.onDestroy()
        requireActivity().loadProgreesBar.visibility = View.GONE
    }


}
