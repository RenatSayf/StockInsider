package com.renatsayf.stockinsider.ui.tracking

import android.opengl.Visibility
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TrackingFragmentBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.custom.TickersView
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.tracking.companies.CompaniesFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.tracking_fragment) {

    companion object {
        val ARG_IS_EDIT = "${this::class.java.simpleName}.isEdit"
        val ARG_SET = "${this::class.java.simpleName}.set"
        val ARG_TITLE = "${this::class.java.simpleName}.title"
    }

    private lateinit var binding: TrackingFragmentBinding
    private val mainVM: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val trackingVM: TrackingListViewModel by lazy {
        ViewModelProvider(this)[TrackingListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tracking_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = TrackingFragmentBinding.bind(view)

        (activity as MainActivity).supportActionBar?.hide()

        val title = arguments?.getString(ARG_TITLE)

        with(binding) {

            traded.tradedTitle.visibility = View.GONE
            traded.tradedMaxET.visibility = View.GONE
            traded.tradedMinET.visibility = View.GONE

            includedToolBar.appToolbar.title = title

            includedToolBar.appToolbar.setNavigationOnClickListener {
                requireActivity().findNavController(R.id.nav_host_fragment).popBackStack()
            }

            mainVM.companies.observe(viewLifecycleOwner) { companies ->
                companies?.let {
                    val tickersListAdapter = TickersListAdapter(requireActivity(), it)
                    tickersView.setAdapter(tickersListAdapter)
                }
            }

            arguments?.let { bundle ->
                val set = bundle.getSerializable(ARG_SET) as RoomSearchSet
                binding.tickersView.setContentText(set.ticker)

                val flag = bundle.getBoolean(ARG_IS_EDIT)
                trackingVM.setState(TrackingListViewModel.State.Edit(flag))
            }

            trackingVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    is TrackingListViewModel.State.Edit -> {
                        enableEditing(state.flag)
                    }
                }
            }

            tickersView.setOnShowAllClickListener(object : TickersView.Listener {
                override fun onShowAllClick(list: List<String>) {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .add(R.id.fragmentContainer, CompaniesFragment::class.java, Bundle())
                        .addToBackStack(null)
                        //.setCustomAnimations()
                        .commit()
                }
            })


        }


    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        (activity as MainActivity).supportActionBar?.show()
        super.onStop()
    }

    private fun enableEditing(flag: Boolean) {
        with(binding){
            if (flag) {
                tickersView.editable = true
                traded.purchaseCheckBox.isClickable = true
                traded.saleCheckBox.isClickable = true
                insider.apply {
                    directorCheBox.isClickable = true
                    officerCheBox.isClickable = true
                    owner10CheBox.isClickable = true
                }
            } else {
                tickersView.editable = false
                traded.purchaseCheckBox.isClickable = false
                traded.saleCheckBox.isClickable = false
                insider.apply {
                    directorCheBox.isClickable = false
                    officerCheBox.isClickable = false
                    owner10CheBox.isClickable = false
                }
            }
            sorting.groupSpinner.apply {
                setSelection(1)
                isEnabled = false
            }
            sorting.sortSpinner.apply {
                setSelection(3)
                isEnabled = false
            }

        }
    }

}