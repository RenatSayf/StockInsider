@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.tracking

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TrackingFragmentBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.custom.TickersView
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.tracking.companies.CompaniesFragment
import com.renatsayf.stockinsider.ui.tracking.companies.CompaniesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList


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

    private val trackingVM: TrackingListViewModel by activityViewModels()
    private val companiesVM: CompaniesViewModel by activityViewModels()

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

            toolbar.title = title

            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            mainVM.companies.observe(viewLifecycleOwner) { companies ->
                companies?.let {
                    val tickersListAdapter = TickersListAdapter(requireActivity(), it)
                    tickersView.setAdapter(tickersListAdapter)
                }
            }

            var setId = "Unnamed"
            arguments?.let { bundle ->
                val set = bundle.getSerializable(ARG_SET) as RoomSearchSet
                val newSet = set.copy()

                setId = set.queryName

                companiesVM.state.observe(viewLifecycleOwner) { state ->
                    when(state) {
                        is CompaniesViewModel.State.Error -> {

                        }
                        is CompaniesViewModel.State.Initial -> {
                            if (state.ticker.isNotEmpty()) {
                                binding.tickersView.setContentText(state.ticker)
                            }
                            else {
                                binding.tickersView.setContentText(set.ticker)
                            }
                        }
                        CompaniesViewModel.State.OnAdding -> {

                        }
                    }
                }

                val flag = bundle.getBoolean(ARG_IS_EDIT)
                trackingVM.setState(TrackingListViewModel.State.Edit(flag))

                traded.purchaseCheckBox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                        newSet.isPurchase = isChecked
                        trackingVM.setState(TrackingListViewModel.State.OnEdit(newSet))
                    }
                })

                trackingVM.state.observe(viewLifecycleOwner) { state ->
                    when(state) {
                        is TrackingListViewModel.State.Edit -> {
                            enableEditing(state.flag)
                        }
                        is TrackingListViewModel.State.Initial -> {

                        }
                        is TrackingListViewModel.State.OnEdit -> {
                            enableSaveButton(set, state.set)
                            trackingVM.setState(TrackingListViewModel.State.Edit(flag))
                        }
                    }
                }

                tickersView.setListeners(object : TickersView.Listener {
                    override fun onShowAllClick(list: List<String>) {
                        val tickersStr = binding.tickersView.contentText
                        val newBundle = Bundle().apply {
                            putStringArrayList(CompaniesFragment.ARG_TICKERS_LIST, list.toMutableList() as ArrayList<String>)
                            putString(CompaniesFragment.ARG_TICKERS_STR, tickersStr)
                            putString(CompaniesFragment.ARG_SET_ID, setId)
                        }
                        findNavController().navigate(R.id.action_trackingFragment_to_companiesFragment, newBundle, null, null)
                    }

                    override fun onContentChanged(text: String) {
                        newSet.ticker = text
                        trackingVM.setState(TrackingListViewModel.State.OnEdit(newSet))
                    }

                    override fun onContentCleared() {
                        newSet.ticker = ""
                        trackingVM.setState(TrackingListViewModel.State.OnEdit(newSet))
                    }
                })
            }



        }
    }

    private fun enableSaveButton(oldSet: RoomSearchSet, newSet: RoomSearchSet) {
        if (oldSet != newSet) {
            binding.btnSave.visibility = View.VISIBLE
        }
        else {
            binding.btnSave.visibility = View.INVISIBLE
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

    override fun onDestroy() {

        companiesVM.setState(CompaniesViewModel.State.Initial(""))
        super.onDestroy()
    }

    private fun enableEditing(flag: Boolean) {
        with(binding){

            tickersView.editable = flag
            traded.purchaseCheckBox.isClickable = flag
            traded.saleCheckBox.isClickable = flag
            insider.apply {
                directorCheBox.isClickable = flag
                officerCheBox.isClickable = flag
                owner10CheBox.isClickable = flag
            }

            sorting.groupSpinner.apply {
                setSelection(1)
                isEnabled = flag
            }
            sorting.sortSpinner.apply {
                setSelection(3)
                isEnabled = flag
            }
        }
    }

}