@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.renatsayf.stockinsider.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.viewbinding.ViewBinding
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.DealLayoutBinding
import com.renatsayf.stockinsider.databinding.FakeDealLayoutBinding
import com.renatsayf.stockinsider.databinding.ItemGroupHeaderBinding
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.GroupHead
import com.renatsayf.stockinsider.models.BaseDeal
import com.renatsayf.stockinsider.models.Skeleton
import com.renatsayf.stockinsider.ui.sorting.SortingViewModel
import java.text.NumberFormat
import java.util.*
import kotlin.collections.List


class DealListAdapter(
    private val listener: Listener? = null
) : Adapter<DealListAdapter.ViewHolder>()
{
    private var context: Context? = null
    private var sorting: SortingViewModel.Sorting? = null

    private var dealsList = mutableListOf<BaseDeal>()

    fun showSkeleton() {
        val skeletonList = List(10, init = {
            Skeleton(it)
        })
        dealsList.clear()
        dealsList.addAll(skeletonList)
        notifyItemRangeChanged(0, this.itemCount)
    }

    fun addItems(map: Map<String, List<Deal>>, sorting: SortingViewModel.Sorting) {

        this.sorting = sorting
        map.forEach { (key, list) ->
            dealsList.add(GroupHead(key))
            dealsList.addAll(list)
        }
        notifyItemRangeChanged(0, this.itemCount)
    }

    fun replaceItems(map: Map<String, List<Deal>>, sorting: SortingViewModel.Sorting) {
        dealsList.clear()
        addItems(map, sorting)
    }

    fun getAllItems(): MutableList<BaseDeal> {
        return dealsList
    }

    fun getDeals(): List<Deal> {
        return dealsList.filterIsInstance<Deal>()
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            dealsList[position] is Deal -> 1
            dealsList[position] is GroupHead -> 0
            else -> -1
        }
    }

    override fun getItemId(position: Int): Long {
        return this.getItemId(position)
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        return when(viewType) {
            0 -> {
                this.context = parent.context
                val headerBinding = ItemGroupHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(headerBinding)
            }
            1 -> {
                this.context = parent.context
                val binding = DealLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding)
            }
            else -> {
                val fakeBinding = FakeDealLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(fakeBinding)
            }
        }
    }

    override fun getItemCount(): Int {
        return dealsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewType = holder.itemViewType
        when (viewType) {
            1 -> {
                holder.bindToDeal(dealsList[position] as Deal)
            }
            0 -> {
                val head = dealsList[position] as GroupHead
                holder.bindToTitle(head.text)
            }
            -1 -> {}
            else -> throw Exception("************* Unknown viewType: $viewType **************")
        }
    }

    interface Listener {
        fun onRecyclerViewItemClick(deal: Deal)
    }

    inner class ViewHolder(private val binding : ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindToTitle(title: String) {
            if (binding is ItemGroupHeaderBinding) {

                val tvTitle = itemView.findViewById<TextView>(R.id.tv_title)

                when (sorting?.groupingBy) {
                    SortingViewModel.Sorting.GroupingBy.FILLING_DATE -> {
                        context?.let {
                            val text = "${it.getString(R.string.text_filing_date)}: $title"
                            tvTitle.text = text
                        }
                    }
                    SortingViewModel.Sorting.GroupingBy.TICKER -> {
                        context?.let {
                            val text = "${it.getString(R.string.text_company_name)}: $title"
                            tvTitle.text = text
                        }
                    }
                    SortingViewModel.Sorting.GroupingBy.INSIDER -> {
                        context?.let {
                            val text = "${it.getString(R.string.text_insider_name)}: $title"
                            tvTitle.text = text
                        }
                    }
                    else -> {}
                }
            }
        }

        fun bindToDeal(deal: BaseDeal) {
            if (binding is DealLayoutBinding) {
                deal as Deal
                binding.filingDateTV.text = deal.filingDate
                binding.tickerTV.text = deal.ticker
                binding.companyNameTV.text = deal.company
                binding.tradeTypeTV.text = deal.tradeType
                val numberFormat = NumberFormat.getInstance(Locale.getDefault())
                val formattedVolume = numberFormat.format(deal.volume)
                binding.dealValueTV.text = formattedVolume
                binding.insiderNameTV.text = deal.insiderName
                binding.insiderTitleTV.text = deal.insiderTitle

                val context = binding.dealConstraintLayout.context

                if (deal.tradeTypeInt > 0) {
                    if (deal.volume <= 999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy1000))
                    }
                    if (deal.volume > 999 && deal.volume <= 9999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy10000))
                    }
                    if (deal.volume > 9999 && deal.volume <= 99999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy100000))
                    }
                    if (deal.volume > 99999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy1000000))
                    }
                } else if (deal.tradeTypeInt == -1) {
                    if (deal.volume <= 999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale1000))
                    }
                    if (deal.volume > 999 && deal.volume <= 9999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale10000))
                    }
                    if (deal.volume > 9999 && deal.volume <= 99999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale100000))
                    }
                    if (deal.volume > 99999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale1000000))
                    }
                } else if (deal.tradeTypeInt == -2) {
                    if (deal.volume <= 999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe1000))
                    }
                    if (deal.volume > 999 && deal.volume <= 9999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe10000))
                    }
                    if (deal.volume > 9999 && deal.volume <= 99999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe100000))
                    }
                    if (deal.volume > 99999) {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe1000000))
                    }
                }

                binding.dealConstraintLayout.setOnClickListener {
                    val defaultColor = binding.dealCardView.cardBackgroundColor.defaultColor
                    deal.color = defaultColor
                    listener?.onRecyclerViewItemClick(deal)
                }
            }
        }

    }

}


