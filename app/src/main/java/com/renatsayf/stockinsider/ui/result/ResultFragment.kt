package com.renatsayf.stockinsider.ui.result

import android.content.Context
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentResultBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.service.InsiderWorker
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.ui.dialogs.SaveSearchDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.AppCalendar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class ResultFragment : Fragment(R.layout.fragment_result), ConfirmationDialog.Listener
{
    companion object
    {
        val TAG = this::class.java.simpleName.toString().plus("_tag")
        val ARG_SEARCH_SET = this::class.java.simpleName.plus(".search_set_tag")
        val ARG_QUERY_NAME = this::class.java.simpleName.toString().plus("_arg_set_name")
        val ARG_TITLE = this::class.java.simpleName.toString().plus("_arg_title")
    }

    private lateinit var binding: FragmentResultBinding
    private lateinit var resultVM : ResultViewModel
    private lateinit var mainViewModel : MainViewModel
    private lateinit var saveDialogObserver : SaveSearchDialog.EventObserver
    private lateinit var searchSet: SearchSet
    private lateinit var roomSearchSet: RoomSearchSet

    private val searchRequest: SearchRequest by lazy {
        SearchRequest()
    }

    private val notification : ServiceNotification by lazy {
        ServiceNotification()
    }

    private val appCalendar: AppCalendar by lazy {
        val timeZone = TimeZone.getTimeZone(requireContext().getString(R.string.app_time_zone))
        AppCalendar(timeZone)
    }

    private val confirmationDialog: ConfirmationDialog by lazy {
        ConfirmationDialog().apply {
            setOnClickListener(this@ResultFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        resultVM = ViewModelProvider(this)[ResultViewModel::class.java]
        saveDialogObserver = ViewModelProvider(activity as MainActivity)[SaveSearchDialog.EventObserver::class.java]

        if (savedInstanceState == null)
        {
            val queryName = arguments?.getString(ARG_QUERY_NAME)


            if (!queryName.isNullOrEmpty())
            {
                val mainActivity = activity as MainActivity

                CoroutineScope(Dispatchers.Main).launch {
                    roomSearchSet = mainViewModel.getSearchSetAsync(queryName)
                    searchSet = SearchSet(roomSearchSet.queryName).apply {
                        ticker = mainActivity.getTickersString(roomSearchSet.ticker)
                        filingPeriod = mainActivity.getFilingOrTradeValue(roomSearchSet.filingPeriod)
                        tradePeriod = mainActivity.getFilingOrTradeValue(roomSearchSet.tradePeriod)
                        isPurchase = mainActivity.getCheckBoxValue(roomSearchSet.isPurchase)
                        isSale = mainActivity.getCheckBoxValue(roomSearchSet.isSale)
                        tradedMin = roomSearchSet.tradedMin
                        tradedMax = roomSearchSet.tradedMax
                        isOfficer = mainActivity.getOfficerValue(roomSearchSet.isOfficer)
                        isDirector = mainActivity.getCheckBoxValue(roomSearchSet.isDirector)
                        isTenPercent = mainActivity.getCheckBoxValue(roomSearchSet.isTenPercent)
                        groupBy = mainActivity.getGroupingValue(roomSearchSet.groupBy)
                        sortBy = mainActivity.getSortingValue(roomSearchSet.sortBy)
                    }
                    mainViewModel.getDealList(searchSet)
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this){
            roomSearchSet.queryName = requireContext().getString(R.string.text_current_set_name)
            CoroutineScope(Dispatchers.Main).launch {
                mainViewModel.saveSearchSet(roomSearchSet)
                (activity as MainActivity).navController.navigate(R.id.nav_home)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentResultBinding.bind(view)



        //region Показываем фейковый список пока подгружаются данные
        val fakeList = ArrayList<Deal>()
        for (i in 0..10) fakeList.add(Deal("xxxxxxxx"))

        resultVM.state.observe(viewLifecycleOwner, { state ->
            when(state)
            {
                is ResultViewModel.State.Initial -> {
                    binding.noResult.noResultLayout.visibility = View.GONE
                    binding.loadProgressLayout.loadProgreesBar.visibility = View.VISIBLE
                }
            }
        })

        binding.tradeListRV.apply {
            val linearManager = LinearLayoutManager(activity)
            val dealListAdapter = DealListAdapter(fakeList, R.layout.fake_deal_layout)
            setHasFixedSize(true)
            layoutManager = linearManager
            adapter = dealListAdapter
        }
        //endregion

        resultVM.addAlarmVisibility.observe(viewLifecycleOwner, {
            it?.let{ binding.alertLayout.addAlarmImgView.visibility = it }
        })

        resultVM.alarmOnVisibility.observe(viewLifecycleOwner, {
            it?.let{
                binding.alertLayout.alarmOnImgView.visibility = it
            }
        })

        val title = arguments?.getString(ARG_TITLE) ?: ""
        resultVM.setToolBarTitle(title)

        mainViewModel.dealList.observe(viewLifecycleOwner, {
            it.let { list ->
                binding.loadProgressLayout.loadProgreesBar.visibility = View.GONE
                when
                {
                    list.size > 0 && list[0].error!!.isEmpty()     ->
                    {
                        binding.saveSearchBtnView.visibility = View.VISIBLE
                        binding.resultTV.text = list.size.toString()
                        val linearLayoutManager = LinearLayoutManager(activity)
                        val dealListAdapter = DealListAdapter(list)
                        dealListAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                        binding.tradeListRV.apply {
                            setHasFixedSize(true)
                            layoutManager = linearLayoutManager
                            adapter = dealListAdapter
                        }
                        return@let
                    }
                    list.size == 1 && list[0].error!!.isNotEmpty() ->
                    {
                        binding.resultTV.text = 0.toString()
                        binding.noResult.recommendationsTV.text = requireContext().getString(R.string.text_data_not_avalible)
                        binding.noResult.noResultLayout.visibility = View.VISIBLE
                    }
                    else                                           ->
                    {
                        binding.resultTV.text = list.size.toString()
                        binding.noResult.noResultLayout.visibility = View.VISIBLE
                    }
                }
            }
        })

        mainViewModel.documentError.observe(viewLifecycleOwner, {
            binding.loadProgressLayout.loadProgreesBar.visibility = View.GONE
            when (it) {
                is IndexOutOfBoundsException ->
                {
                    val dealList: ArrayList<Deal> = arrayListOf()
                    when
                    {
                        dealList.size > 0 && dealList[0].error!!.isEmpty()     ->
                        {
                            binding.resultTV.text = dealList.size.toString()
                            val linearLayoutManager = LinearLayoutManager(activity)
                            val dealListAdapter = DealListAdapter(dealList)
                            dealListAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                            binding.tradeListRV.apply {
                                setHasFixedSize(true)
                                layoutManager = linearLayoutManager
                                adapter = dealListAdapter
                            }
                        }
                        dealList.size == 1 && dealList[0].error!!.isNotEmpty() ->
                        {
                            with(binding) {
                                resultTV.text = 0.toString()
                                noResult.recommendationsTV.text = requireContext().getString(R.string.text_data_not_avalible)
                                noResult.noResultLayout.visibility = View.VISIBLE
                            }
                        }
                        else                                                   ->
                        {
                            with(binding) {
                                resultTV.text = dealList.size.toString()
                                noResult.noResultLayout.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                else ->
                {
                    if (it != null)
                    {
                        if ((activity as MainActivity).isNetworkConnectivity())
                        {
                            Snackbar.make(binding.tradeListRV, it.message.toString(), Snackbar.LENGTH_LONG).show()
                        }
                        else
                        {
                            with(binding) {
                                resultTV.text = 0.toString()
                                noResult.recommendationsTV.text = requireContext().getString(R.string.text_data_not_avalible)
                                noResult.noResultLayout.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        })

        binding.alertLayout.addAlarmImgView.setOnClickListener {
            confirmationDialog.apply {
                message = getString(R.string.text_confirm_search)
                flag = ""
            }.show(parentFragmentManager, ConfirmationDialog.TAG)
        }

        binding.alertLayout.alarmOnImgView.setOnClickListener {
            confirmationDialog.apply {
                message = getString(R.string.text_cancel_search)
                flag = ConfirmationDialog.FLAG_CANCEL
            }.show(parentFragmentManager, ConfirmationDialog.TAG)
        }

        binding.noResult.backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        activity?.let{ a ->
            when ((a as MainActivity).isServiceRunning())
            {
                true ->
                {
                    resultVM.setAddAlarmVisibility(View.GONE)
                    resultVM.setAlarmOnVisibility(View.VISIBLE)
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
                            resultVM.setAddAlarmVisibility(View.VISIBLE)
                            resultVM.setAlarmOnVisibility(View.GONE)
                            Snackbar.make(binding.tradeListRV, msg, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })

        binding.saveSearchBtnView.setOnClickListener {
            val name = (if (searchSet.ticker.isEmpty()) "All" else searchSet.ticker)
                .plus("/period"+searchSet.filingPeriod)
                .plus(if (searchSet.isPurchase == "1") "/Pur" else "")
                .plus(if (searchSet.isSale == "1") "/Sale" else "")
                .plus(if (searchSet.tradedMin.isNotEmpty()) "/min${searchSet.tradedMin}" else "")
                .plus(if (searchSet.tradedMax.isNotEmpty()) "/max${searchSet.tradedMax}" else "")
                .plus(if (searchSet.isOfficer.isNotEmpty()) "/Officer" else "")
                .plus(if (searchSet.isDirector == "1") "/Dir" else "")
                .plus(if (searchSet.isTenPercent == "1") "/10%Owner" else "")
            SaveSearchDialog.getInstance(name).show(requireActivity().supportFragmentManager, SaveSearchDialog.TAG)
        }

        saveDialogObserver.data.observe(viewLifecycleOwner, { event ->
            event.getContent()?.let {
                if (it.first == SaveSearchDialog.KEY_SEARCH_NAME)
                {
                    roomSearchSet.queryName = it.second
                    CoroutineScope(Dispatchers.Main).launch {
                        mainViewModel.saveSearchSet(roomSearchSet)
                    }
                }
            }
        })
    }

    override fun onDestroy()
    {
        super.onDestroy()
        binding.loadProgressLayout.loadProgreesBar.visibility = View.GONE
    }

    override fun onPositiveClick(flag: String)
    {
        when (flag)
        {
            ConfirmationDialog.FLAG_CANCEL ->
            {
                activity?.let{ a ->
                    (a as MainActivity).setAlarmSetting(false)
                    a.getString(R.string.text_search_is_disabled).let { msg ->
                        resultVM.setAddAlarmVisibility(View.VISIBLE)
                        resultVM.setAlarmOnVisibility(View.GONE)
                        Snackbar.make(binding.tradeListRV, msg, Snackbar.LENGTH_LONG).show()
                    }
                }
                WorkManager.getInstance(requireContext()).cancelAllWork()
            }
            else ->
            {
                context?.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)?.edit {
                    putBoolean(AlarmPendingIntent.IS_ALARM_SETUP_KEY, true)
                    apply()
                }
                resultVM.setAddAlarmVisibility(View.GONE)
                resultVM.setAlarmOnVisibility(View.VISIBLE)
                context?.getString(R.string.text_searching_is_created)?.let { msg ->
                    Snackbar.make(binding.alertLayout.alarmOnImgView, msg, Snackbar.LENGTH_LONG).show()
                }

                WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(InsiderWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, InsiderWorker.periodicWorkRequest)
            }
        }
    }


}
