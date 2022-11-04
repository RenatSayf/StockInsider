package com.renatsayf.stockinsider.ui.tracking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.setVisible
import com.renatsayf.stockinsider.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
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

        if (savedInstanceState == null) {
            binding.includeProgress.loadProgressBar.setVisible(true)
            val tracking = Target.Tracking
            mainVM.getSearchSetsByTarget(tracking).observe(viewLifecycleOwner) { list ->
                trackingVM.setState(TrackingListViewModel.State.Initial(list))
            }
        }

        mainVM.state.observe(viewLifecycleOwner) { state ->
            when(state) {
                is MainViewModel.State.Initial -> TODO()
            }
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

        ConfirmationDialog(
            message = "Подтвердите удаление",
            btnOkText = "Удалить",
        ).apply {
            setOnClickListener(object : ConfirmationDialog.Listener {

                override fun onPositiveClick(flag: String) {
                    lifecycleScope.launchWhenResumed {
                        mainVM.deleteSearchSet(set).collect { res ->
                            if (res > 0) {
                                mainVM.getSearchSetsByTarget(Target.Tracking).observe(this@TrackingListFragment.viewLifecycleOwner) { list ->
                                    trackingVM.setState(TrackingListViewModel.State.Initial(list))
                                }
                            }
                        }
                    }
                }
            })
        }.show(requireActivity().supportFragmentManager, ConfirmationDialog.TAG)
    }

    override fun onTrackingAdapterSwitcherOnChange(set: RoomSearchSet, checked: Boolean, position: Int) {

        lifecycleScope.launchWhenResumed {
            set.isTracked = checked
            mainVM.saveSearchSet(set).collect { id ->
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