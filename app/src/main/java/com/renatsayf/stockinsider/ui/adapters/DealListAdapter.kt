package com.renatsayf.stockinsider.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Deal
import kotlinx.android.synthetic.main.deal_layout.view.*

class DealListAdapter(private val dealList : ArrayList<Deal>) : RecyclerView.Adapter<DealListAdapter.ViewHolder>()
{
    private lateinit var context: Context
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val cardView = LayoutInflater.from(parent.context).inflate(
                R.layout.deal_layout,
                parent,
                false) as CardView
        context = parent.context
        return ViewHolder(cardView)
    }

    override fun getItemCount() : Int
    {
        return dealList.size
    }

    override fun onBindViewHolder(holder : ViewHolder, position : Int)
    {
        val itemView = holder.itemView
        val deal = dealList[position]
        itemView.filingDateTV.text = deal.filingDate
        itemView.tickerTV.text = deal.ticker
        itemView.companyNameTV.text = deal.company
        itemView.tradeTypeTV.text = deal.tradeType
        itemView.dealValueTV.text = deal.volume.toString()
        itemView.insiderNameTV.text = deal.insiderName
        itemView.insiderTitleTV.text = deal.insiderTitle
        if (deal.tradeTypeInt > 0)
        {
            if (deal.volume <= 999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_buy1000)
            }
            if (deal.volume > 999 && deal.volume <= 9999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_buy10000)
            }
            if (deal.volume > 9999 && deal.volume <= 99999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_buy100000)
            }
            if (deal.volume > 99999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_buy1000000)
            }
        }
        else if (deal.tradeTypeInt == -1)
        {
            if (deal.volume <= 999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_sale1000)
            }
            if (deal.volume > 999 && deal.volume <= 9999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_sale10000)
            }
            if (deal.volume > 9999 && deal.volume <= 99999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_sale100000)
            }
            if (deal.volume > 99999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_sale1000000)
            }
        }
        else if (deal.tradeTypeInt == -2)
        {
            if (deal.volume <= 999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_sale_oe1000)
            }
            if (deal.volume > 999 && deal.volume <= 9999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_sale_oe10000)
            }
            if (deal.volume > 9999 && deal.volume <= 99999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_sale_oe100000)
            }
            if (deal.volume > 99999)
            {
                itemView.dealConstrLayout.background = context.resources.getDrawable(R.drawable.selector_sale_oe1000000)
            }
        }
    }

}


