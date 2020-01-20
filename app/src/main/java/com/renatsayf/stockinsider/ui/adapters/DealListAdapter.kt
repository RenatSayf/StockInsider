package com.renatsayf.stockinsider.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.R
import kotlinx.android.synthetic.main.deal_layout.view.*
import org.jsoup.Jsoup

class DealListAdapter(private val dealList : ArrayList<String>) : RecyclerView.Adapter<DealListAdapter.ViewHolder>()
{
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {

    }

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
        val strItem = dealList.get(position)
        val document = Jsoup.parse(strItem)
        val filingDate = document.select("td:nth-child(2) > div > a")
        holder.itemView.filingDateTV.text = filingDate.toString()
    }

}