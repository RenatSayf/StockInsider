@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.tracking.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Spinner
import androidx.core.view.forEach
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.databinding.TrackingFragmentBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Target.Tracking
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.dialogs.SetNameDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.tracking.companies.CompaniesViewModel
import com.renatsayf.stockinsider.utils.getSerializableCompat
import com.renatsayf.stockinsider.utils.px
import com.renatsayf.stockinsider.utils.setVisible
import com.renatsayf.stockinsider.utils.showSnackBar
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

    private val trackingVM: TrackingViewModel by activityViewModels()
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

        val title = arguments?.getString(ARG_TITLE)

        with(binding) {

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

            arguments?.let { bundle ->
                val currentSet = bundle.getSerializableCompat(ARG_SET, RoomSearchSet::class.java)
                if (savedInstanceState == null && trackingVM.newSet == null) {
                    trackingVM.newSet = currentSet?.copy()
                }

                companiesVM.setState(CompaniesViewModel.State.Initial(trackingVM.newSet?.ticker ?: ""))
                trackingVM.newSet?.let { fillLayoutData(it) }

                companiesVM.state.observe(viewLifecycleOwner) { state ->
                    when(state) {
                        is CompaniesViewModel.State.Error -> {
                            showSnackBar(state.message)
                        }
                        is CompaniesViewModel.State.Initial -> {
                            if (state.ticker.isNotEmpty()) {
                                includeTickersView.contentTextView.setText(state.ticker)
                            }
                            else {
                                includeTickersView.contentTextView.setText(currentSet?.ticker)
                            }
                        }
                        CompaniesViewModel.State.OnAdding -> {}
                        is CompaniesViewModel.State.OnUpdate -> {}
                        is CompaniesViewModel.State.Current -> {

                        }
                    }
                }

                val flag = bundle.getBoolean(ARG_IS_EDIT)
                trackingVM.setState(TrackingViewModel.State.Edit(flag))

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
                                    if (!set.isPurchase && !set.isSale) {
                                        set.apply {
                                            isPurchase = true
                                            isSale = true
                                        }
                                    }
                                    trackingVM.setState(TrackingViewModel.State.OnEdit(set))
                                    binding.scrollView.smoothScrollTo(0, binding.btnSave.bottom)
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
                                    trackingVM.setState(TrackingViewModel.State.OnEdit(set))
                                    binding.scrollView.smoothScrollTo(0, binding.btnSave.bottom)
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
                                    trackingVM.setState(TrackingViewModel.State.OnEdit(set))
                                }
                            }
                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                    }
                }

                trackingVM.state.observe(viewLifecycleOwner) { state ->
                    when(state) {
                        is TrackingViewModel.State.Edit -> {
                            enableEditing(state.flag)
                        }
                        is TrackingViewModel.State.OnEdit -> {
                            val newSet = trackingVM.newSet
                            newSet?.let { set ->
                                fillLayoutData(set)
                                currentSet?.let {
                                    enableSaveButton(it, set)
                                }
                            }
                            trackingVM.setState(TrackingViewModel.State.Edit(flag))
                        }
                        is TrackingViewModel.State.OnSave -> {
                            enableSaveButton(state.set, state.set)
                            showSnackBar("Изменения сохранены")
                            trackingVM.setState(TrackingViewModel.State.Edit(flag))
                            trackingVM.newSet = null
                        }
                    }
                }

                includeSetName.etSetName.doOnTextChanged { text, _, before, count ->
                    if (before != count) {
                        trackingVM.newSet?.let { set ->
                            set.queryName = text.toString()
                            trackingVM.setState(TrackingViewModel.State.OnEdit(set))
                        }
                    }
                }

                var tickerText = ""
                includeTickersView.contentTextView.apply {
                    doOnTextChanged { text, _, before, count ->
                        if (text != null) {
                            if (text.isNotEmpty()) {
                                val c = text[text.length - 1]
                                if (c.isWhitespace()) {
                                    tickerText = text.toString()
                                    this.setSelection((count))
                                }
                            } else {
                                tickerText = ""
                            }

                            if (before != count) {
                                trackingVM.newSet?.let { set ->
                                    set.ticker = text.toString()
                                    trackingVM.setState(TrackingViewModel.State.OnEdit(set))
                                }
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

                traded.tradedMinET.doOnTextChanged { text, _, before, count ->
                    if (before != count) {
                        trackingVM.newSet?.let { set ->
                            set.tradedMin = text.toString()
                            trackingVM.setState(TrackingViewModel.State.OnEdit(set))
                        }
                    }
                }
                traded.tradedMaxET.doOnTextChanged { text, _, before, count ->
                    if (before != count) {
                        trackingVM.newSet?.let { set ->
                            set.tradedMax = text.toString()
                            trackingVM.setState(TrackingViewModel.State.OnEdit(set))
                        }
                    }
                }

                includeTickersView.btnShow.setOnClickListener {
                    val tickersStr = binding.includeTickersView.contentTextView.text.toString()
                    companiesVM.setState(CompaniesViewModel.State.Initial(tickersStr))
                    findNavController().navigate(R.id.action_trackingFragment_to_companiesFragment)
                }
                includeTickersView.clearTextBtn.setOnClickListener {
                    trackingVM.newSet?.let { set ->
                        set.ticker = ""
                        trackingVM.setState(TrackingViewModel.State.OnEdit(set))
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

                    SetNameDialog(set, listener = object : SetNameDialog.Listener {
                        override fun onSetNameDialogPositiveClick(name: String) {
                            newSet.queryName = name
                            lifecycleScope.launchWhenResumed {
                                mainVM.saveSearchSet(newSet).collect { id ->
                                    when {
                                        id > 0L -> {
                                            trackingVM.getSearchSetById(id).collect { set ->
                                                if (set != null) {
                                                    trackingVM.setState(TrackingViewModel.State.OnSave(set))
                                                    findNavController().popBackStack()
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
                    }).show(requireActivity().supportFragmentManager, SetNameDialog.TAG)
                }
            }
        }

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val heightDiff: Int = binding.root.rootView.height - binding.root.height
                val threshold: Int = 200.px
                val result = heightDiff > threshold
                if (!result && binding.btnSave.visibility == View.VISIBLE) {
                    binding.scrollView.smoothScrollTo(0, binding.btnSave.bottom)
                }
            }
        })
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

    private fun enableEditing(flag: Boolean) {
        with(binding){

            includeSetName.etSetName.isEnabled = flag
            includeTickersView.contentTextView.apply {
                isEnabled = flag
            }
            includeTickersView.clearTextBtn.setVisible(flag)

            traded.apply {
                traded.purchaseCheckBox.isClickable = flag
                traded.saleCheckBox.isClickable = flag
                tradedMinET.isEnabled = flag
                tradedMaxET.isEnabled = flag
            }

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

            includeSetName.etSetName.apply {
                setText(set.queryName)
            }

            includeTickersView.contentTextView.setText(set.ticker)

            traded.apply {
                purchaseCheckBox.isChecked = set.isPurchase
                saleCheckBox.isChecked = set.isSale
                tradedMinET.apply {
                    setText(set.tradedMin)
                    setSelection(this.text.length)
                }
                tradedMaxET.apply {
                    setText(set.tradedMax)
                    setSelection(this.text.length)
                }
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

    override fun onDestroy() {

        trackingVM.newSet = null
        super.onDestroy()
    }
}