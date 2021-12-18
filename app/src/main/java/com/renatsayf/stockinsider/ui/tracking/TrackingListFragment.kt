package com.renatsayf.stockinsider.ui.tracking

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.databinding.TrackingListFragmentBinding
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.ui.adapters.TrackingAdapter
import com.renatsayf.stockinsider.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackingListFragment : Fragment(), TrackingAdapter.Listener {

    private val mainVM: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private val trackingVM: TrackingListViewModel by lazy {
        ViewModelProvider(this)[TrackingListViewModel::class.java]
    }

    private lateinit var binding: TrackingListFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TrackingListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            mainVM.getSearchSetsByTarget(Target.Tracking).observe(viewLifecycleOwner, { list ->
                trackingVM.setState(TrackingListViewModel.State.Initial(list))
            })
        }

        trackingVM.state.observe(viewLifecycleOwner, { state ->
            when(state) {
                is TrackingListViewModel.State.Initial -> {
                    with(binding) {
                        trackersRV.apply {
                            setHasFixedSize(true)
                            layoutManager = LinearLayoutManager(requireContext())
                            adapter = TrackingAdapter(state.list, this@TrackingListFragment).apply {
                                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onTrackingAdapterEditButtonClick() {
        Toast.makeText(requireContext(), "BBBBBBBBBBBBBB", Toast.LENGTH_SHORT).show()
    }

    override fun onTrackingAdapterDeleteButtonClick() {
        Toast.makeText(requireContext(), "AAAAAAAAAAAAAAAA", Toast.LENGTH_SHORT).show()
    }


}