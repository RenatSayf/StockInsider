package com.renatsayf.stockinsider.ui.tracking.list

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
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
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.ui.adapters.TrackingAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.ui.dialogs.InfoDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.settings.askForPermission
import com.renatsayf.stockinsider.ui.tracking.item.TrackingFragment
import com.renatsayf.stockinsider.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


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
        this.registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
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
                    trackingAdapter.submitList(state.list as MutableList<RoomSearchSet>)
                    binding.includeProgress.loadProgressBar.setVisible(false)
                }
                else -> binding.includeProgress.loadProgressBar.setVisible(false)
            }
        }

        binding.addButton.setOnClickListener {

            trackingVM.targetCount().observe(viewLifecycleOwner) { count ->
                count?.let {
                    if (it < FireBaseConfig.requestsCount) {
                        val set = RoomSearchSet(
                            queryName = "",
                            companyName = "",
                            ticker = "",
                            filingPeriod = 1,
                            tradePeriod = 3,
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
            }
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
        ).show(requireActivity().supportFragmentManager, ConfirmationDialog.TAG)
    }

    override fun onTrackingAdapterSwitcherOnChange(set: RoomSearchSet, checked: Boolean, position: Int) {

        set.isTracked = checked
        mainVM.saveSearchSet(set).observe(viewLifecycleOwner) { id ->
            if (id != null && id > 0) {
                when (checked) {
                    true -> {
                        showSnackBar(getString(R.string.text_tracking_enabled))
                        requireActivity().showOrNotInfoDialog {
                            showInfoDialog(
                                title = getString(R.string.text_warning),
                                message = "\"${getString(R.string.text_manufacturer_of_devices)} ${Build.MANUFACTURER}, ${
                                    getString(
                                        R.string.text_battery_restrictions_message
                                    )
                                }\"",
                                status = InfoDialog.DialogStatus.EXTENDED_WARNING,
                                callback = {
                                    when(it) {
                                        1 -> {
                                            appPref.edit().putBoolean(InfoDialog.KEY_NOT_SHOW_AGAN, true).apply()
                                            this@TrackingListFragment.openAppSystemSettings()
                                        }
                                        0 -> {
                                            appPref.edit().putBoolean(InfoDialog.KEY_NOT_SHOW_AGAN, true).apply()
                                        }
                                    }
                                }
                            )
                        }
                    }
                    else -> {
                        showSnackBar(getString(R.string.text_tracking_disabled))
                        trackingVM.trackedCount().observe(viewLifecycleOwner) { count ->
                            if (count == 0) {
                                val scheduler = Scheduler(requireContext().applicationContext)
                                val pendingIntent = scheduler.isAlarmSetup(false)
                                pendingIntent?.let {
                                    scheduler.cancel(it)
                                }
                            }
                        }
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
        InfoDialog.newInstance(title = getString(R.string.text_info), message = infoText, InfoDialog.DialogStatus.INFO).let { dialog ->
            dialog.show(requireActivity().supportFragmentManager, dialog.tag)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.hide()

        binding.includedToolBar.appToolbar.apply {
            title = getString(R.string.text_tracking_list)
            setNavigationOnClickListener {
                checkNotificationPermission(
                    onGranted = {
                        parentFragmentManager.popBackStack()
                    }
                )
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                checkNotificationPermission(
                    onGranted = {
                        parentFragmentManager.popBackStack()
                    }
                )
            }
        })
    }

    override fun onDestroyView() {

        (activity as MainActivity).supportActionBar?.show()
        super.onDestroyView()
    }

    fun checkNotificationPermission(
        onGranted: () -> Unit
    ) {
        trackingVM.trackedCount().observe(viewLifecycleOwner) { count ->
            count?.let {
                if (it > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        askForPermission(
                            Manifest.permission.POST_NOTIFICATIONS,
                            onInit = {
                                InfoDialog.newInstance(
                                    title = getString(R.string.text_warning),
                                    message = getString(R.string.text_for_notification_permission),
                                    status = InfoDialog.DialogStatus.WARNING,
                                    callback = {i ->
                                        if (i > 0) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                        else {
                                            onGranted.invoke()
                                        }
                                    }
                                ).show(parentFragmentManager, InfoDialog.TAG)
                            },
                            onGranted = {
                                onGranted.invoke()
                            }
                        )
                    }
                    else {
                        onGranted.invoke()
                    }
                }
                else {
                    onGranted.invoke()
                }
            }
        }
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