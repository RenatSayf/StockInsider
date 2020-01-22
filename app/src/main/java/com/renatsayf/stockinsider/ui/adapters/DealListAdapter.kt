package com.renatsayf.stockinsider.ui.adapters

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
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : ViewHolder
    {
        val cardView = LayoutInflater.from(parent.context).inflate(
                R.layout.deal_layout,
                parent,
                false) as CardView
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
    }

}