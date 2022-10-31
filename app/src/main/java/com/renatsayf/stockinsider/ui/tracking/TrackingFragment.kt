@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.tracking

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Spinner
import androidx.core.view.forEach
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.databinding.TrackingFragmentBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Target.Tracking
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.tracking.companies.CompaniesFragment
import com.renatsayf.stockinsider.ui.tracking.companies.CompaniesViewModel
import com.renatsayf.stockinsider.utils.setVisible
import com.renatsayf.stockinsider.utils.showSnackBar
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
                    includeTickersView.contentTextView.setAdapter(tickersListAdapter)
                }
            }

            var setId = "Unnamed"
            arguments?.let { bundle ->
                val set = bundle.getSerializable(ARG_SET) as RoomSearchSet
                trackingVM.newSet = set.copy()

                setId = set.queryName
                fillLayoutData(set)

                companiesVM.state.observe(viewLifecycleOwner) { state ->
                    when(state) {
                        is CompaniesViewModel.State.Error -> {

                        }
                        is CompaniesViewModel.State.Initial -> {
                            if (state.ticker.isNotEmpty()) {
                                includeTickersView.contentTextView.setText(state.ticker)
                            }
                            else {
                                includeTickersView.contentTextView.setText(set.ticker)
                            }
                        }
                        CompaniesViewModel.State.OnAdding -> {

                        }
                    }
                }

                val flag = bundle.getBoolean(ARG_IS_EDIT)
                trackingVM.setState(TrackingListViewModel.State.Edit(flag))

                traded.root.forEach {
                    if (it is CheckBox) {
                        it.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                            override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
                                trackingVM.newSet?.let { set ->
                                    when(p0?.id) {
                                        R.id.purchaseCheckBox -> {
                                            set.isPurchase = isChecked
                                        }
                                        R.id.saleCheckBox -> {
                                            set.isSale = isChecked
                                        }
                                    }
                                    trackingVM.setState(TrackingListViewModel.State.OnEdit(set))
                                }
                            }
                        })
                    }
                }
                insider.root.forEach {
                    if (it is CheckBox) {
                        it.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                            override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
                                trackingVM.newSet?.let { set ->
                                    when(p0?.id) {
                                        R.id.officer_CheBox -> {
                                            set.isOfficer = isChecked
                                        }
                                        R.id.director_CheBox -> {
                                            set.isDirector = isChecked
                                        }
                                        R.id.owner10_CheBox -> {
                                            set.isTenPercent = isChecked
                                        }
                                    }
                                    trackingVM.setState(TrackingListViewModel.State.OnEdit(set))
                                }
                            }
                        })
                    }
                }

                sorting.root.forEach {
                    if (it is Spinner) {
                        it.onItemSelectedListener = object : OnItemSelectedListener {
                            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                trackingVM.newSet?.let { set ->
                                    when(p0?.id) {
                                        R.id.group_spinner -> {
                                            set.groupBy = p2
                                        }
                                        R.id.sort_spinner -> {
                                            set.sortBy = p2
                                        }
                                    }
                                    trackingVM.setState(TrackingListViewModel.State.OnEdit(set))
                                }
                            }
                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                    }
                }

                trackingVM.state.observe(viewLifecycleOwner) { state ->
                    when(state) {
                        is TrackingListViewModel.State.Edit -> {
                            enableEditing(state.flag)
                        }
                        is TrackingListViewModel.State.Initial -> {

                        }
                        is TrackingListViewModel.State.OnEdit -> {
                            updateSearchSetName(state.set)
                            enableSaveButton(set, state.set)
                            trackingVM.setState(TrackingListViewModel.State.Edit(flag))
                        }
                        is TrackingListViewModel.State.OnSave -> {
                            updateSearchSetName(state.set)
                            enableSaveButton(state.set, state.set)
                            showSnackBar("Изменения сохранены")
                            trackingVM.setState(TrackingListViewModel.State.Edit(flag))
                        }
                    }
                }

                var tickerText = ""
                includeTickersView.contentTextView.apply {
                    doOnTextChanged { text, start, before, count ->
                        if (text != null) {
                            if (text.isNotEmpty()) {
                                val c = text[text.length - 1]
                                if (c.isWhitespace()) {
                                    tickerText = text.toString()
                                }
                            } else {
                                tickerText = ""
                            }
                            this.setSelection((count))
                            trackingVM.newSet?.let { set ->
                                set.ticker = text.toString()
                                trackingVM.setState(TrackingListViewModel.State.OnEdit(set))
                            }
                        }

                    }
                    onItemClickListener = object : OnItemClickListener {
                        override fun onItemClick(p0: AdapterView<*>?, v: View?, p2: Int, p3: Long) {
                            v?.let {
                                val tickerLayout = TickerLayoutBinding.bind(v)
                                val ticker = tickerLayout.tickerTV.text
                                val str = (tickerText.plus(ticker)).trim()
                                this@apply.setText(str)
                                this@apply.setSelection(str.length)
                                includeTickersView.btnShow.visibility = View.VISIBLE
                            }
                        }

                    }
                }
                includeTickersView.btnShow.setOnClickListener {
                    val list = if (includeTickersView.contentTextView.text.isEmpty()) listOf()
                    else includeTickersView.contentTextView.text.toString().split(" ")
                    val tickersStr = binding.includeTickersView.contentTextView.text.toString()
                    val newBundle = Bundle().apply {
                        putStringArrayList(CompaniesFragment.ARG_TICKERS_LIST, list.toMutableList() as ArrayList<String>)
                        putString(CompaniesFragment.ARG_TICKERS_STR, tickersStr)
                        putString(CompaniesFragment.ARG_SET_ID, setId)
                    }
                    findNavController().navigate(R.id.action_trackingFragment_to_companiesFragment, newBundle, null, null)
                }
                includeTickersView.clearTextBtn.setOnClickListener {
                    trackingVM.newSet?.let { set ->
                        set.ticker = ""
                        trackingVM.setState(TrackingListViewModel.State.OnEdit(set))
                    }

                }
            }

            btnSave.setOnClickListener {
                val newSet = trackingVM.newSet
                newSet?.let {
                    it.queryName = includeSetName.etSetName.text.toString()
                    it.target = Tracking
                    it.isTracked = true
                }
                newSet?.let { set ->
                    lifecycleScope.launchWhenResumed {
                        mainVM.saveSearchSet(set).collect { id ->
                            when {
                                id > 0L -> {
                                    mainVM.getSearchSetList().observe(viewLifecycleOwner) { list ->
                                        val setById = list.firstOrNull {
                                            it.id == trackingVM.newSet?.id
                                        }
                                        setById?.let {
                                            trackingVM.setState(TrackingListViewModel.State.OnSave(it))
                                        }
                                    }
                                }
                                id == 0L -> {
                                    showSnackBar("Не удалось сохранить изменения")
                                }
                            }
                        }
                    }
                }
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

            includeSetName.etSetName.isEnabled = flag
            includeTickersView.contentTextView.apply {
                isEnabled = flag
            }
            includeTickersView.clearTextBtn.setVisible(flag)
            traded.purchaseCheckBox.isClickable = flag
            traded.saleCheckBox.isClickable = flag
            insider.apply {
                directorCheBox.isClickable = flag
                officerCheBox.isClickable = flag
                owner10CheBox.isClickable = flag
            }

            sorting.groupSpinner.apply {
                isEnabled = flag
            }
            sorting.sortSpinner.apply {
                isEnabled = flag
            }
        }
    }

    private fun fillLayoutData(set: RoomSearchSet) {
        with(binding) {

            includeSetName.etSetName.setText(set.queryName)

            traded.apply {
                purchaseCheckBox.isChecked = set.isPurchase
                saleCheckBox.isChecked = set.isSale
            }

            insider.apply {
                officerCheBox.isChecked = set.isOfficer
                directorCheBox.isChecked = set.isDirector
                owner10CheBox.isChecked = set.isTenPercent
            }

            sorting.groupSpinner.apply {
                setSelection(trackingVM.newSet?.groupBy ?: 0)
            }
            sorting.sortSpinner.apply {
                setSelection(trackingVM.newSet?.sortBy ?: 0)
            }
        }
    }

    private fun updateSearchSetName(set: RoomSearchSet) {

        with(binding) {
            val newName = set.generateQueryName()
            includeSetName.etSetName.setText(newName)
            includeTickersView.contentTextView.setText(set.ticker)
        }
    }

}