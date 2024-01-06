@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.renatsayf.stockinsider.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
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
import com.renatsayf.stockinsider.models.BaseDeal
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.GroupHead
import com.renatsayf.stockinsider.models.Skeleton
import com.renatsayf.stockinsider.ui.sorting.SortingViewModel
import com.renatsayf.stockinsider.utils.setVisible
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.*


class DealListAdapter(
    private val listener: Listener? = null
) : Adapter<DealListAdapter.ViewHolder>()
{
    private var context: Context? = null
    private var sorting: SortingViewModel.Sorting? = null

    private var dealsList = mutableListOf<BaseDeal>()
    private val skeletonList = List(10, init = {
        Skeleton(it)
    })

    fun showSkeleton() {
        dealsList.clear()
        dealsList.addAll(skeletonList)
        notifyItemRangeChanged(0, dealsList.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addItems(map: Map<String, List<Deal>>, sorting: SortingViewModel.Sorting) {

        this.sorting = sorting
        map.forEach { (key, list) ->
            dealsList.add(GroupHead(key))
            dealsList.addAll(list)
        }
        notifyDataSetChanged()
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
            dealsList.isNotEmpty() && dealsList[position] is Deal -> 1
            dealsList.isNotEmpty() && dealsList[position] is GroupHead -> 0
            else -> -1
        }
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
                            val text = "${it.getString(R.string.text_ticker)}: $title"
                            tvTitle.text = text
                        }
                    }
                    SortingViewModel.Sorting.GroupingBy.INSIDER -> {
                        context?.let {
                            val text = "${it.getString(R.string.text_insider)} $title"
                            tvTitle.text = text
                        }
                    }
                    else -> {}
                }
            }
        }

        fun bindToDeal(deal: BaseDeal) {
            if (binding is DealLayoutBinding) {
                with(binding){
                    deal as Deal
                    filingDateTV.text = deal.filingDate
                    tickerTV.text = deal.ticker
                    companyNameTV.text = deal.company
                    tradeTypeTV.text = deal.tradeType
                    val numberFormat = NumberFormat.getInstance(Locale.getDefault())
                    val formattedVolume = numberFormat.format(deal.volume)
                    dealValueTV.text = formattedVolume
                    insiderNameTV.text = deal.insiderName
                    insiderTitleTV.text = deal.insiderTitle
                    tvPriceValue.text = deal.price

                    val context = dealConstraintLayout.context

                    val uri = Uri.parse(deal.tickerRefer)
                    Picasso.get().load(uri)
                        .placeholder(R.drawable.image_area_chart_144dp)
                        .into(ivStockChart, object : Callback {
                        override fun onSuccess() {
                            progressChart.setVisible(false)
                        }

                        override fun onError(e: java.lang.Exception?) {
                            progressChart.setVisible(false)
                        }
                    })

                    if (deal.tradeTypeInt > 0) {
                        if (deal.volume <= 999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.buy1000))
                        }
                        if (deal.volume > 999 && deal.volume <= 9999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.buy10000))
                        }
                        if (deal.volume > 9999 && deal.volume <= 99999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.buy100000))
                        }
                        if (deal.volume > 99999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.buy1000000))
                        }
                    } else if (deal.tradeTypeInt == -1) {
                        if (deal.volume <= 999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.sale1000))
                        }
                        if (deal.volume > 999 && deal.volume <= 9999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.sale10000))
                        }
                        if (deal.volume > 9999 && deal.volume <= 99999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.sale100000))
                        }
                        if (deal.volume > 99999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.sale1000000))
                        }
                    } else if (deal.tradeTypeInt == -2) {
                        if (deal.volume <= 999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe1000))
                        }
                        if (deal.volume > 999 && deal.volume <= 9999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe10000))
                        }
                        if (deal.volume > 9999 && deal.volume <= 99999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe100000))
                        }
                        if (deal.volume > 99999) {
                            dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe1000000))
                        }
                    }

                    dealConstraintLayout.setOnClickListener {
                        val defaultColor = dealCardView.cardBackgroundColor.defaultColor
                        deal.color = defaultColor
                        listener?.onRecyclerViewItemClick(deal)
                    }
                }
            }
        }

    }

}


