package com.renatsayf.stockinsider.ui.tracking.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TrackingListFragmentBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.schedule.IScheduler
import com.renatsayf.stockinsider.ui.adapters.TrackingAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.tracking.item.TrackingFragment
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.setVisible
import com.renatsayf.stockinsider.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class TrackingListFragment : Fragment(), TrackingAdapter.Listener {

    private lateinit var binding: TrackingListFragmentBinding

    @Inject
    lateinit var scheduler: IScheduler

    @Inject
    lateinit var calendar: AppCalendar

    private val mainVM: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private val trackingVM: TrackingListViewModel by lazy {
        ViewModelProvider(this)[TrackingListViewModel::class.java]
    }

    private val trackingAdapter: TrackingAdapter by lazy {
        TrackingAdapter(listener = this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TrackingListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.includedToolBar.appToolbar.apply {
            title = getString(R.string.text_tracking_list)
            setNavigationOnClickListener {
                requireActivity().findNavController(R.id.nav_host_fragment).popBackStack()
            }
        }

        binding.trackersRV.apply {
            setHasFixedSize(true)
            adapter = trackingAdapter
        }

        binding.includeProgress.loadProgressBar.setVisible(true)
        val tracking = Target.Tracking
        mainVM.getSearchSetsByTarget(tracking).observe(viewLifecycleOwner) { list ->
            trackingVM.setState(TrackingListViewModel.State.Initial(list))
        }

        trackingVM.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TrackingListViewModel.State.Initial -> {
                    trackingAdapter.submitList(state.list as MutableList<RoomSearchSet>)
                    binding.includeProgress.loadProgressBar.setVisible(false)
                }
                else -> binding.includeProgress.loadProgressBar.setVisible(false)
            }
        }

        binding.addButton.setOnClickListener {
            val set = RoomSearchSet(
                queryName = "",
                companyName = "",
                ticker = "",
                filingPeriod = 3,
                tradePeriod = 1,
                tradedMin = "",
                tradedMax = "",
                isOfficer = true,
                isDirector = true,
                isTenPercent = true,
                groupBy = 1,
                sortBy = 3
            )
            findNavController().navigate(R.id.action_trackingListFragment_to_trackingFragment, Bundle().apply {
                putSerializable(TrackingFragment.ARG_SET, set)
                putString(TrackingFragment.ARG_TITLE, "Отслеживание. Новый поиск")
                putBoolean(TrackingFragment.ARG_IS_EDIT, true)
            })
        }

    }

    override fun onTrackingAdapterEditButtonClick(set: RoomSearchSet, position: Int) {
        findNavController().navigate(R.id.action_trackingListFragment_to_trackingFragment, Bundle().apply {
            putSerializable(TrackingFragment.ARG_SET, set)
            putString(TrackingFragment.ARG_TITLE, "Отслеживание. Редактирование")
            putBoolean(TrackingFragment.ARG_IS_EDIT, true)
        })
    }

    override fun onTrackingAdapterDeleteButtonClick(set: RoomSearchSet, position: Int) {

        ConfirmationDialog.newInstance(
            message = "Подтвердите удаление",
            positiveButtonText = "Удалить",
            listener = object : ConfirmationDialog.Listener {
                override fun onPositiveClick() {
                    lifecycleScope.launch {
                        mainVM.deleteSearchSetById(set.id).collectLatest { res ->
                            when {
                                res > 0 -> {
                                    mainVM.getSearchSetsByTarget(Target.Tracking).asFlow().collectLatest { list ->
                                        trackingVM.setState(TrackingListViewModel.State.Initial(list))
                                    }
                                }
                                res == 0 -> {
                                    showSnackBar(getString(R.string.text_deletion_error))
                                }
                            }
                        }
                    }
                }
            }
        ).show(requireActivity().supportFragmentManager, ConfirmationDialog.TAG)
    }

    override fun onTrackingAdapterSwitcherOnChange(set: RoomSearchSet, checked: Boolean, position: Int) {

        lifecycleScope.launchWhenResumed {
            set.isTracked = checked
            mainVM.saveSearchSet(set).observe(viewLifecycleOwner) { id ->
                if (id > 0) {
                    when(checked) {
                        true -> showSnackBar("Отслеживание включено")
                        else -> showSnackBar("Отслеживание выключено")
                    }
                }
            }
        }
    }

    override fun onTrackingAdapterVisibilityButtonClick(set: RoomSearchSet, position: Int) {
        findNavController().navigate(R.id.action_trackingListFragment_to_trackingFragment, Bundle().apply {
            putSerializable(TrackingFragment.ARG_SET, set)
            putString(TrackingFragment.ARG_TITLE, "Отслеживание. Просмотр")
            putBoolean(TrackingFragment.ARG_IS_EDIT, false)
        })
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        (activity as MainActivity).supportActionBar?.show()
        super.onStop()
    }


}