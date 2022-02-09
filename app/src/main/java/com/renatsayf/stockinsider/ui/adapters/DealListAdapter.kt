@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.renatsayf.stockinsider.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.DealLayoutBinding
import com.renatsayf.stockinsider.databinding.FakeDealLayoutBinding
import com.renatsayf.stockinsider.models.Deal
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList



class DealListAdapter(private val dealList: ArrayList<Deal>,
                        private val listener: Listener? = null) : RecyclerView.Adapter<DealListAdapter.ViewHolder>()
{
    private lateinit var binding: DealLayoutBinding
    private var skeletonList = ArrayList<Deal>().apply {
        repeat(10) {
            add(Deal(null))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            dealList.isNotEmpty() -> 1
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        return when(viewType) {
            1 -> {
                binding = DealLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding)
            }
            else -> {
                val fakeBinding = FakeDealLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(fakeBinding)
            }
        }
    }

    override fun getItemCount() : Int
    {
        return when {
            dealList.isEmpty() -> skeletonList.size
            else -> dealList.size
        }
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        val viewType = holder.itemViewType
        if (viewType == 1) {
            ViewHolder(binding).bind(dealList[position])
        }
    }

    interface Listener {
        fun onRecyclerViewItemClick(deal: Deal)
    }

    inner class ViewHolder(private val binding : ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(deal: Deal) {
            if (binding is DealLayoutBinding) {
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

                if (deal.tradeTypeInt > 0)
                {
                    if (deal.volume <= 999)
                    {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy1000))
                    }
                    if (deal.volume > 999 && deal.volume <= 9999)
                    {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy10000))
                    }
                    if (deal.volume > 9999 && deal.volume <= 99999)
                    {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy100000))
                    }
                    if (deal.volume > 99999)
                    {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy1000000))
                    }
                }
                else if (deal.tradeTypeInt == -1)
                {
                    if (deal.volume <= 999)
                    {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale1000))
                    }
                    if (deal.volume > 999 && deal.volume <= 9999)
                    {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale10000))
                    }
                    if (deal.volume > 9999 && deal.volume <= 99999)
                    {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale100000))
                    }
                    if (deal.volume > 99999)
                    {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale1000000))
                    }
                }
                else if (deal.tradeTypeInt == -2)
                {
                    if (deal.volume <= 999)
                    {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe1000))
                    }
                    if (deal.volume > 999 && deal.volume <= 9999)
                    {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe10000))
                    }
                    if (deal.volume > 9999 && deal.volume <= 99999)
                    {
                        binding.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe100000))
                    }
                    if (deal.volume > 99999)
                    {
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


