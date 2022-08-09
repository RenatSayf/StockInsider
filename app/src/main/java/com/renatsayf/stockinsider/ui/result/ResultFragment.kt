package com.renatsayf.stockinsider.ui.result

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentResultBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.deal.DealFragment
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.ui.dialogs.SaveSearchDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class ResultFragment : Fragment(R.layout.fragment_result), ConfirmationDialog.Listener, DealListAdapter.Listener, SaveSearchDialog.Listener {
    companion object
    {
        val TAG = this::class.java.simpleName.toString().plus("_tag")
        val ARG_SEARCH_SET = this::class.java.simpleName.plus(".search_set_tag")
        val ARG_TITLE = this::class.java.simpleName.toString().plus("_arg_title")
    }

    private lateinit var binding: FragmentResultBinding
    private lateinit var resultVM : ResultViewModel
    private lateinit var mainViewModel : MainViewModel
    private lateinit var roomSearchSet: RoomSearchSet

    private val dealsAdapter: DealListAdapter by lazy {
        DealListAdapter(this@ResultFragment)
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

        if (savedInstanceState == null)
        {
            val title = arguments?.getString(ARG_TITLE)
            (activity as MainActivity).supportActionBar?.title = title
            roomSearchSet = arguments?.getSerializable(ARG_SEARCH_SET) as RoomSearchSet

            roomSearchSet.let {
                resultVM.getDealList(it.toSearchSet())
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this){
            (activity as MainActivity).navController.popBackStack()
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

        binding.tradeListRV.apply {
            adapter = dealsAdapter.apply {
                showSkeleton()
            }
        }

        resultVM.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultViewModel.State.Initial -> {
                    binding.noResult.noResultLayout.visibility = View.GONE
                    binding.includedProgress.visibility = View.VISIBLE
                }

                is ResultViewModel.State.DataReceived -> {
                    state.deals.let { list ->
                        binding.noResult.noResultLayout.visibility = View.GONE
                        binding.includedProgress.visibility = View.GONE
                        when {
                            list.size > 0 && list[0].error!!.isEmpty() -> {
                                binding.btnAddToTracking.visibility = View.VISIBLE
                                binding.resultTV.text = list.size.toString()
                                binding.tradeListRV.apply {
                                    dealsAdapter.addItems(list)
                                    adapter = dealsAdapter
                                }
                                return@let
                            }
                            list.size == 1 && list[0].error!!.isNotEmpty() -> {
                                binding.resultTV.text = 0.toString()
                                binding.noResult.recommendationsTV.text = requireContext().getString(R.string.text_data_not_avalible)
                                binding.noResult.noResultLayout.visibility = View.VISIBLE
                            }
                            else -> {
                                binding.resultTV.text = list.size.toString()
                                binding.noResult.noResultLayout.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                is ResultViewModel.State.DataError -> {
                    state.throwable.let {
                        binding.includedProgress.visibility = View.GONE
                        when (it) {
                            is IndexOutOfBoundsException -> {
                                with(binding) {
                                    resultTV.text = "0"
                                    noResult.noResultLayout.visibility = View.VISIBLE
                                }
                            }
                            else -> {
                                if ((activity as MainActivity).isNetworkConnectivity()) {
                                    Snackbar.make(binding.tradeListRV, it.message.toString(), Snackbar.LENGTH_LONG).show()
                                } else {
                                    with(binding) {
                                        resultTV.text = 0.toString()
                                        noResult.recommendationsTV.text = requireContext().getString(R.string.text_data_not_avalible)
                                        noResult.noResultLayout.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }



        resultVM.addAlarmVisibility.observe(viewLifecycleOwner) {
            it?.let { binding.alertLayout.addAlarmImgView.visibility = it }
        }

        resultVM.alarmOnVisibility.observe(viewLifecycleOwner) {
            it?.let {
                binding.alertLayout.alarmOnImgView.visibility = it
            }
        }

        val title = arguments?.getString(ARG_TITLE) ?: ""
        resultVM.setToolBarTitle(title)

        binding.alertLayout.addAlarmImgView.setOnClickListener {
            confirmationDialog.apply {
                message = this@ResultFragment.getString(R.string.text_confirm_search)
                flag = ""
            }.show(parentFragmentManager, ConfirmationDialog.TAG)
        }

        binding.alertLayout.alarmOnImgView.setOnClickListener {
            confirmationDialog.apply {
                message = this@ResultFragment.getString(R.string.text_cancel_search)
                flag = ConfirmationDialog.FLAG_CANCEL
            }.show(parentFragmentManager, ConfirmationDialog.TAG)
        }

        binding.noResult.backButton.setOnClickListener {
            activity?.onBackPressed()
        }



        StockInsiderService.serviceEvent.observe(viewLifecycleOwner) { event ->
            if (!event.hasBeenHandled) {
                when (event.getContent()) {
                    StockInsiderService.STOP_KEY -> {
                        context?.getString(R.string.text_search_is_disabled)?.let { msg ->
                            resultVM.setAddAlarmVisibility(View.VISIBLE)
                            resultVM.setAlarmOnVisibility(View.GONE)
                            Snackbar.make(binding.tradeListRV, msg, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        binding.btnAddToTracking.setOnClickListener {
            val name = (roomSearchSet.ticker.ifEmpty { "All" })
                .plus("/period"+roomSearchSet.filingPeriod)
                .plus(if (roomSearchSet.isPurchase) "/Pur" else "")
                .plus(if (roomSearchSet.isSale) "/Sale" else "")
                .plus(if (roomSearchSet.tradedMin.isNotEmpty()) "/min${roomSearchSet.tradedMin}" else "")
                .plus(if (roomSearchSet.tradedMax.isNotEmpty()) "/max${roomSearchSet.tradedMax}" else "")
                .plus(if (roomSearchSet.isOfficer) "/Officer" else "")
                .plus(if (roomSearchSet.isDirector) "/Dir" else "")
                .plus(if (roomSearchSet.isTenPercent) "/10%Owner" else "")
            SaveSearchDialog.getInstance(name, listener = this).show(requireActivity().supportFragmentManager, SaveSearchDialog.TAG)
        }

    }

    override fun onDestroy()
    {
        super.onDestroy()
        binding.includedProgress.visibility = View.GONE
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
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onRecyclerViewItemClick(deal: Deal) {
        val bundle = Bundle()
        bundle.putParcelable(DealFragment.ARG_DEAL, deal)
        (activity as MainActivity).findNavController(R.id.nav_host_fragment).navigate(R.id.nav_deal, bundle)
    }

    override fun saveSearchDialogOnPositiveClick(searchName: String) {

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {

            roomSearchSet.apply {
                id = 0
                queryName = searchName
                isTracked = true
                target = Target.Tracking
            }

            mainViewModel.saveSearchSet(roomSearchSet).observe(viewLifecycleOwner) {
                if (it > 0) {
                    showSnackBar(getString(R.string.text_search_param_is_saved))
                }
                else showSnackBar("Saving error...")
            }
        }
    }


}
