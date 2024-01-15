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
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.databinding.TrackingFragmentBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.dialogs.SetNameDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.tracking.companies.CompaniesFragment
import com.renatsayf.stockinsider.ui.tracking.companies.CompaniesViewModel
import com.renatsayf.stockinsider.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.tracking_fragment), SetNameDialog.Listener {

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

        with(binding) {

            toolbar.title = arguments?.getString(ARG_TITLE)
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            mainVM.companies.observe(viewLifecycleOwner) { companies ->
                companies?.let {
                    val tickersListAdapter = TickersListAdapter(requireActivity(), it)
                    includeTickersView.contentTextView.setAdapter(tickersListAdapter)
                }
            }

            var copySet: RoomSearchSet?
            arguments?.let { bundle ->
                val currentSet = bundle.getSerializableCompat(ARG_SET, RoomSearchSet::class.java)
                if (savedInstanceState == null) {
                    copySet = currentSet?.getFullCopy()
                    val flag = bundle.getBoolean(ARG_IS_EDIT)
                    copySet?.let {
                        trackingVM.setState(TrackingViewModel.State.Initial(it, flag))
                    }
                }

                parentFragmentManager.setFragmentResultListener(KEY_FRAGMENT_RESULT, viewLifecycleOwner) { key, bundle1 ->
                    if (key == KEY_FRAGMENT_RESULT) {
                        val tickersString = bundle1.getString(CompaniesFragment.ARG_TICKERS, "")
                        val newSet = trackingVM.newSet
                        newSet?.let {
                            it.ticker = tickersString
                            trackingVM.setState(TrackingViewModel.State.OnEdit(it))
                        }
                    }
                }

                traded.root.forEach {
                    if (it is CheckBox) {
                        it.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                            override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
                                val newSet = trackingVM.newSet
                                newSet?.let { set ->
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
                                val newSet = trackingVM.newSet
                                newSet?.let { set ->
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
                                val newSet = trackingVM.newSet
                                newSet?.let { set ->
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
                        is TrackingViewModel.State.OnEdit -> {
                            val newSet = trackingVM.newSet
                            newSet?.let { set ->
                                fillLayoutData(set)
                                enableSaveButton(currentSet, set)
                            }
                        }
                        is TrackingViewModel.State.OnSave -> {
                            enableSaveButton(state.set, state.set)
                            showSnackBar("Изменения сохранены")
                            findNavController().popBackStack()
                        }
                        is TrackingViewModel.State.Initial -> {
                            val set = state.set
                            val editFlag = state.editFlag
                            fillLayoutData(set)
                            enableEditing(editFlag)
                        }
                    }
                }

                includeSetName.etSetName.doOnTextChanged { text, _, before, count ->
                    if (before != count) {
                        val newSet = trackingVM.newSet
                        newSet?.let { set ->
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
                                val newSet = trackingVM.newSet
                                newSet?.let { set ->
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
                        val newSet = trackingVM.newSet
                        newSet?.let { set ->
                            set.tradedMin = text.toString()
                            trackingVM.setState(TrackingViewModel.State.OnEdit(set))
                        }
                    }
                }
                traded.tradedMaxET.doOnTextChanged { text, _, before, count ->
                    if (before != count) {
                        val newSet = trackingVM.newSet
                        newSet?.let { set ->
                            set.tradedMax = text.toString()
                            trackingVM.setState(TrackingViewModel.State.OnEdit(set))
                        }
                    }
                }

                includeTickersView.btnShow.setOnClickListener {
                    val tickersStr = binding.includeTickersView.contentTextView.text.toString()
                    companiesVM.setState(CompaniesViewModel.State.Initial(tickersStr))
                    findNavController().navigate(R.id.action_trackingFragment_to_companiesFragment, Bundle().apply {
                        val editingFlag = bundle.getBoolean(ARG_IS_EDIT)
                        putBoolean(ARG_IS_EDIT, editingFlag)
                    })
                }
                includeTickersView.clearTextBtn.setOnClickListener {
                    val newSet = trackingVM.newSet
                    newSet?.let { set ->
                        set.ticker = ""
                        trackingVM.setState(TrackingViewModel.State.OnEdit(set))
                    }

                }
            }

            btnSave.setOnClickListener {
                val newSet = trackingVM.newSet?.copy()
                newSet?.let {
                    SetNameDialog.newInstance(it, listener = this@TrackingFragment).show(requireActivity().supportFragmentManager, SetNameDialog.TAG)
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

    private fun enableSaveButton(oldSet: RoomSearchSet?, newSet: RoomSearchSet?) {
        if (oldSet != newSet) {
            binding.btnSave.setVisible(true)
        }
        else {
            binding.btnSave.setVisible(false)
        }
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

            includeTickersView.contentTextView.apply {
                setText(set.ticker)
                setSelection(this.text.length)
            }

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
                setSelection(set.groupBy)
            }
            sorting.sortSpinner.apply {
                setSelection(set.sortBy)
            }
        }
    }

    override fun onSetNameDialogPositiveClick(name: String) {
        val newSet = trackingVM.newSet?.copy()
        newSet?.let { set ->
            set.queryName = name
            set.isTracked = true
            set.target = Target.Tracking

            lifecycleScope.launch {
                mainVM.saveSearchSet(set).asFlow().collectLatest { id ->
                    when {
                        id != null && id > 0L -> {
                            trackingVM.setState(TrackingViewModel.State.OnSave(newSet))
                        }
                        id == 0L -> {
                            showSnackBar(getString(R.string.text_failed_to_save))
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        (activity as MainActivity).supportActionBar?.hide()
        super.onResume()
    }

    override fun onDestroyView() {
        (activity as MainActivity).supportActionBar?.show()
        super.onDestroyView()
    }



}