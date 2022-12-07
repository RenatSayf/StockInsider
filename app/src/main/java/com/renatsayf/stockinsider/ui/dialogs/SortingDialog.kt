@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.core.view.forEach
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.DialogSortingBinding
import com.renatsayf.stockinsider.ui.sorting.SortingViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SortingDialog : DialogFragment() {

    companion object {
        val TAG = "${this::class.java.simpleName}.TAG"

        private var dialog: SortingDialog? = null
        private var listener: Listener? = null

        fun instance(listener: Listener): SortingDialog {

            this.listener = listener
            return if (dialog == null) SortingDialog() else dialog!!
        }
    }

    private var binding: DialogSortingBinding? = null
    private var sorting = SortingViewModel.Sorting()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogSortingBinding.inflate(layoutInflater)
        return AlertDialog.Builder(requireContext()).apply {
            setView(binding!!.root)
        }.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_dialog_default)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding?.root ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let { b ->

            when(sorting.groupingBy) {
                SortingViewModel.Sorting.GroupingBy.NOT -> {
                    b.rbNotGroup.isChecked = true
                }
                SortingViewModel.Sorting.GroupingBy.TICKER -> {
                    b.rbCompany.isChecked = true
                }
                SortingViewModel.Sorting.GroupingBy.INSIDER -> {
                    b.rbInsider.isChecked = true
                }
            }
            when(sorting.sortingBy) {
                SortingViewModel.Sorting.SortingBy.FILLING_DATE -> {
                    b.rbFillingDate.isChecked = true
                }
                SortingViewModel.Sorting.SortingBy.TRADE_DATE -> {
                    b.rbTradeDate.isChecked = true
                }
                SortingViewModel.Sorting.SortingBy.TICKER -> {
                    b.rbTicker.isChecked = true
                }
                SortingViewModel.Sorting.SortingBy.VOLUME -> {
                    b.rbDealVolume.isChecked = true
                }
            }

            b.rgGrouping.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
                override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                    when(checkedId) {
                        R.id.rb_company -> {
                            sorting.groupingBy = SortingViewModel.Sorting.GroupingBy.TICKER
                        }
                        R.id.rb_insider -> {
                            sorting.groupingBy = SortingViewModel.Sorting.GroupingBy.INSIDER
                        }
                    }
                }
            })
            b.rgSorting.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
                override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                    when(checkedId) {
                        R.id.rb_trade_date -> {
                            sorting.sortingBy = SortingViewModel.Sorting.SortingBy.TRADE_DATE
                        }
                        R.id.rb_ticker -> {
                            sorting.sortingBy = SortingViewModel.Sorting.SortingBy.TICKER
                        }
                        R.id.rb_deal_volume -> {
                            sorting.sortingBy = SortingViewModel.Sorting.SortingBy.VOLUME
                        }
                    }
                }
            })
            b.tvByAsc.setOnClickListener {
                sorting.orderBy = SortingViewModel.Sorting.OrderBy.ASC
                listener?.onSortingDialogButtonClick(sorting)
                dismiss()
            }
            b.tvByDesc.setOnClickListener {
                sorting.orderBy = SortingViewModel.Sorting.OrderBy.DESC
                listener?.onSortingDialogButtonClick(sorting)
                dismiss()
            }
            b.btnClose.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }

    interface Listener {
        fun onSortingDialogButtonClick(sorting: SortingViewModel.Sorting)
    }
}