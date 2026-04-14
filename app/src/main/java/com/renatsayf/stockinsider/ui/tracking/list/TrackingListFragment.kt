package com.renatsayf.stockinsider.ui.tracking.list

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TrackingListFragmentBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.schedule.cancelReminderAlarm
import com.renatsayf.stockinsider.schedule.isReminderAlarmActive
import com.renatsayf.stockinsider.schedule.setReminderAlarm
import com.renatsayf.stockinsider.ui.adapters.TrackingAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.ui.dialogs.InfoDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.settings.askForPermission
import com.renatsayf.stockinsider.ui.tracking.item.TrackingFragment
import com.renatsayf.stockinsider.utils.appPref
import com.renatsayf.stockinsider.utils.openAppSystemSettings
import com.renatsayf.stockinsider.utils.setVisible
import com.renatsayf.stockinsider.utils.showIfNotAdded
import com.renatsayf.stockinsider.utils.showInfoDialog
import com.renatsayf.stockinsider.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@SuppressLint("NotifyDataSetChanged")
@AndroidEntryPoint
class TrackingListFragment : Fragment(), TrackingAdapter.Listener {

    private lateinit var binding: TrackingListFragmentBinding

    private val mainVM: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private val trackingVM: TrackingListViewModel by lazy {
        ViewModelProvider(this)[TrackingListViewModel::class.java]
    }

    private val trackingAdapter: TrackingAdapter by lazy {
        TrackingAdapter(listener = this)
    }
    private val permissionLauncher: ActivityResultLauncher<String> by lazy {
        this.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                requireContext().cancelReminderAlarm()
                requireContext().setReminderAlarm(FireBaseConfig.trackingPeriod)
            }
            else {
                requireContext().cancelReminderAlarm()
                val sets = trackingVM.trackerList.map { item ->
                    item.isTracked = false
                    item
                }
                trackingAdapter.submitList(sets)
                trackingAdapter.notifyDataSetChanged()

                InfoDialog.newInstance(
                    title = getString(R.string.text_warning),
                    message = getString(R.string.text_for_notification_permission),
                    status = InfoDialog.DialogStatus.WARNING,
                    callback = {i ->
                        if (i > 0) {
                            requireContext().openAppSystemSettings()
                        }
                    }
                ).showIfNotAdded(parentFragmentManager)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TrackingListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("InlinedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionLauncher

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
                    var sets: List<RoomSearchSet> = state.list
                    val permission = requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        sets = sets.map { item ->
                            item.isTracked = false
                            item
                        }
                    }
                    trackingAdapter.submitList(sets as MutableList<RoomSearchSet>)
                    binding.includeProgress.loadProgressBar.setVisible(false)
                }
                else -> binding.includeProgress.loadProgressBar.setVisible(false)
            }
        }

        binding.addButton.setOnClickListener {

            trackingVM.getTrackedCountAsync(
                onSuccess = { count ->
                    if (count < FireBaseConfig.requestsCount) {
                        val set = RoomSearchSet(
                            queryName = "",
                            companyName = "",
                            ticker = "",
                            filingPeriod = 1,
                            tradePeriod = 7,
                            tradedMin = "",
                            tradedMax = "",
                            isOfficer = true,
                            isDirector = true,
                            isTenPercent = true,
                            groupBy = 0,
                            sortBy = 3
                        )
                        findNavController().navigate(R.id.action_trackingListFragment_to_trackingFragment, Bundle().apply {
                            putSerializable(TrackingFragment.ARG_SET, set)
                            putString(TrackingFragment.ARG_TITLE, getString(R.string.text_tracking_new_search))
                            putBoolean(TrackingFragment.ARG_IS_EDIT, true)
                        })
                    }
                    else {
                        showInfoDialog(
                            title = getString(R.string.text_exceeded_max_background_requests),
                            status = InfoDialog.DialogStatus.ERROR
                        )
                    }
                }
            )
        }

    }

    override fun onTrackingAdapterEditButtonClick(set: RoomSearchSet, position: Int) {
        findNavController().navigate(R.id.action_trackingListFragment_to_trackingFragment, Bundle().apply {
            putSerializable(TrackingFragment.ARG_SET, set)
            putString(TrackingFragment.ARG_TITLE, getString(R.string.text_tracking_editing))
            putBoolean(TrackingFragment.ARG_IS_EDIT, true)
        })
    }

    override fun onTrackingAdapterDeleteButtonClick(set: RoomSearchSet, position: Int) {

        ConfirmationDialog.newInstance(
            message = getString(R.string.text_deletion_confirm),
            positiveButtonText = getString(R.string.text_delete),
            listener = object : ConfirmationDialog.Listener {
                override fun onPositiveClick() {
                    lifecycleScope.launch {
                        mainVM.deleteSearchSetById(set.id).collectLatest { res ->
                            when {
                                res > 0 -> {
                                    mainVM.getSearchSetsByTarget(Target.Tracking).asFlow().collectLatest { list ->
                                        trackingVM.setState(TrackingListViewModel.State.Initial(list))
                                        val trackedList = list.filter { it.isTracked }
                                        if (trackedList.isEmpty()) {
                                            requireContext().cancelReminderAlarm()
                                            showSnackBar(getString(R.string.text_tracking_disabled))
                                        }
                                    }
                                }
                                res == 0 -> {
                                    showInfoDialog(title = getString(R.string.text_deletion_error), status = InfoDialog.DialogStatus.ERROR)
                                }
                            }
                        }
                    }
                }
            }
        ).showIfNotAdded(requireActivity().supportFragmentManager)
    }

    override fun onTrackingAdapterSwitcherOnChange(set: RoomSearchSet, isChecked: Boolean, position: Int) {

        set.isTracked = isChecked
        mainVM.saveSearchSet(set).observe(viewLifecycleOwner) { id ->
            if (id != null && id > 0) {
                when (isChecked) {
                    true -> {
                        showSnackBar(getString(R.string.text_tracking_enabled))

                        checkNotificationPermission(
                            onRationale = {
                                InfoDialog.newInstance(
                                    title = getString(R.string.text_warning),
                                    message = getString(R.string.text_for_notification_permission),
                                    status = InfoDialog.DialogStatus.WARNING,
                                    callback = {i ->
                                        if (i > 0) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                        else {
                                            requireContext().cancelReminderAlarm()
                                            var position = -1
                                            val sets = trackingVM.trackerList.mapIndexed { index, item ->
                                                if (set.id == item.id) {
                                                    item.isTracked = false
                                                    position = index
                                                }
                                                item
                                            }
                                            trackingAdapter.submitList(sets)
                                            trackingAdapter.notifyItemChanged(position)
                                        }
                                    }
                                ).showIfNotAdded(parentFragmentManager)
                            },
                            onGranted = {
                                requireContext().cancelReminderAlarm()
                                requireContext().setReminderAlarm(FireBaseConfig.trackingPeriod)
                            },
                            onDenied = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        )
                    }
                    else -> {
                        showSnackBar(getString(R.string.text_tracking_disabled))
                        trackingVM.getTrackedCountAsync(
                            onSuccess = {count ->
                                if (count == 0) {
                                    val alarmActive = requireContext().isReminderAlarmActive()
                                    if (alarmActive) {
                                        requireContext().cancelReminderAlarm()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onTrackingAdapterVisibilityButtonClick(set: RoomSearchSet, position: Int) {
        findNavController().navigate(R.id.action_trackingListFragment_to_trackingFragment, Bundle().apply {
            putSerializable(TrackingFragment.ARG_SET, set)
            putString(TrackingFragment.ARG_TITLE, getString(R.string.text_tracking_viewing))
            putBoolean(TrackingFragment.ARG_IS_EDIT, false)
        })
    }

    override fun onTrackingAdapterInfoButtonClick(set: RoomSearchSet) {
        val queryName = set.queryName
        val infoText = "${getString(R.string.text_tracking_description)} $queryName"
        InfoDialog.newInstance(title = getString(R.string.text_info), message = infoText, InfoDialog.DialogStatus.INFO)
            .showIfNotAdded(requireActivity().supportFragmentManager)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.hide()

        binding.includedToolBar.appToolbar.apply {
            title = getString(R.string.text_tracking_list)
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })
    }

    override fun onDestroyView() {

        (activity as MainActivity).supportActionBar?.show()
        super.onDestroyView()
    }

    fun checkNotificationPermission(
        onRationale: () -> Unit,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        trackingVM.getTrackedCountAsync(
            onSuccess = {count ->
                if (count > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        askForPermission(
                            Manifest.permission.POST_NOTIFICATIONS,
                            onInit = {
                                onRationale.invoke()
                            },
                            onGranted = {
                                onGranted.invoke()
                            },
                            onDenied = {
                                onDenied.invoke()
                            }
                        )
                    }
                    else {
                        onGranted.invoke()
                    }
                }
            },
            onError = { exception ->
                exception.printStackTrace()
            }
        )
    }

}

fun Activity.showOrNotInfoDialog(
    isNotShow: Boolean = appPref.getBoolean(InfoDialog.KEY_NOT_SHOW_AGAN, false),
    manufacturer: String = Build.MANUFACTURER,
    callback: () -> Unit
) {

    val devices = FireBaseConfig.problemDevices.map {
        it.uppercase()
    }
    if (devices.contains(manufacturer.uppercase()) && !isNotShow) {
        callback.invoke()
    }
}