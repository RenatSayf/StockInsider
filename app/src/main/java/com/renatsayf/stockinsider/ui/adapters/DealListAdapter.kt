package com.renatsayf.stockinsider.ui.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.deal.DealFragment
import com.renatsayf.stockinsider.utils.Event
import kotlinx.android.synthetic.main.deal_layout.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class DealListAdapter(private var activity : MainActivity, private val dealList : ArrayList<Deal>) : RecyclerView.Adapter<DealListAdapter.ViewHolder>()
{
    companion object
    {
        val dealAdapterItemClick : MutableLiveData<Event<Drawable>> = MutableLiveData()
    }

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
        val numberFormat = NumberFormat.getInstance(Locale.getDefault())
        val formatedVolume = numberFormat.format(deal.volume)
        itemView.dealValueTV.text = formatedVolume
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

        itemView.dealConstrLayout.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(DealFragment.TAG, deal)
            dealAdapterItemClick.value = Event(it.dealConstrLayout.background)
            activity.findNavController(R.id.nav_host_fragment).navigate(R.id.dealFragment, bundle)
        }
    }

}


