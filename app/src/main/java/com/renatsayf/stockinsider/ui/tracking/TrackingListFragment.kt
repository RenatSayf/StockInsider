package com.renatsayf.stockinsider.ui.tracking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TrackingListFragmentBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.service.InsiderWorker
import com.renatsayf.stockinsider.ui.adapters.TrackingAdapter
import com.renatsayf.stockinsider.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


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
        TrackingAdapter(listener = this).apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
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

        binding.includedToolBar.apply {
            appToolbar.title = getString(R.string.text_tracking_list)
            appToolbar.setNavigationOnClickListener {
                requireActivity().findNavController(R.id.nav_host_fragment).popBackStack()
            }
        }

        binding.trackersRV.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackingAdapter
        }

        if (savedInstanceState == null) {
            mainVM.getSearchSetsByTarget(Target.Tracking).observe(viewLifecycleOwner, { list ->
                trackingVM.setState(TrackingListViewModel.State.Initial(list))
            })
        }

        trackingVM.state.observe(viewLifecycleOwner, { state ->
            when(state) {
                is TrackingListViewModel.State.Initial -> {
                    trackingAdapter.setItems(state.list as MutableList<RoomSearchSet>)
                }
            }
        })

        binding.addButton.setOnClickListener {

        }

    }

    override fun onTrackingAdapterEditButtonClick(set: RoomSearchSet, position: Int) {

    }

    override fun onTrackingAdapterDeleteButtonClick(set: RoomSearchSet, position: Int) {

    }

    override fun onTrackingAdapterSwitcherOnChange(set: RoomSearchSet, checked: Boolean, position: Int) {
        set.isTracked = checked
        mainVM.saveSearchSet(set).observe(viewLifecycleOwner, {
            if (it > 0) {
                trackingAdapter.modifyItem(set, position)
                InsiderWorker.addWorkRequest(set.queryName)
            }
        })

    }

    override fun onTrackingAdapterVisibilityButtonClick(set: RoomSearchSet, position: Int) {
        (activity as MainActivity).findNavController(R.id.nav_host_fragment).navigate(R.id.action_trackingListFragment_to_trackingFragment, Bundle().apply {
            putSerializable(TrackingFragment.ARG_SET, set)
            putString(TrackingFragment.ARG_TITLE, "Отслеживание")
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