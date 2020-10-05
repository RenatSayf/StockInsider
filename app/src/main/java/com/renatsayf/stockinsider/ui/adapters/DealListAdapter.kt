package com.renatsayf.stockinsider.ui.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.deal.DealFragment
import com.renatsayf.stockinsider.utils.AppLog
import com.renatsayf.stockinsider.utils.Event
import kotlinx.android.synthetic.main.deal_layout.view.*
import java.text.NumberFormat
import java.util.*

class DealListAdapter(private val dealList: ArrayList<Deal>,
                        private val childLayoutId: Int) : RecyclerView.Adapter<DealListAdapter.ViewHolder>()
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
                childLayoutId,
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
        when (childLayoutId)
        {
            R.layout.deal_layout      ->
            {
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
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy1000))
                    }
                    if (deal.volume > 999 && deal.volume <= 9999)
                    {
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy10000))
                    }
                    if (deal.volume > 9999 && deal.volume <= 99999)
                    {
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy100000))
                    }
                    if (deal.volume > 99999)
                    {
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.buy1000000))
                    }
                }
                else if (deal.tradeTypeInt == -1)
                {
                    if (deal.volume <= 999)
                    {
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale1000))
                    }
                    if (deal.volume > 999 && deal.volume <= 9999)
                    {
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale10000))
                    }
                    if (deal.volume > 9999 && deal.volume <= 99999)
                    {
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale100000))
                    }
                    if (deal.volume > 99999)
                    {
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale1000000))
                    }
                }
                else if (deal.tradeTypeInt == -2)
                {
                    if (deal.volume <= 999)
                    {
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe1000))
                    }
                    if (deal.volume > 999 && deal.volume <= 9999)
                    {
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe10000))
                    }
                    if (deal.volume > 9999 && deal.volume <= 99999)
                    {
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe100000))
                    }
                    if (deal.volume > 99999)
                    {
                        itemView.dealCardView.setCardBackgroundColor(context.getColor(R.color.sale_oe1000000))
                    }
                }

                itemView.dealConstraintLayout.setOnClickListener {
                    itemView.dealMotionLayout.transitionToEnd()

                }

                itemView.dealMotionLayout.setTransitionListener(object : MotionLayout.TransitionListener{
                    override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float)
                    {

                    }

                    override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int)
                    {

                    }

                    override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float)
                    {

                    }

                    override fun onTransitionCompleted(p0: MotionLayout?, p1: Int)
                    {
                        val bundle = Bundle()
                        bundle.putParcelable(DealFragment.ARG_DEAL, deal)
                        dealAdapterItemClick.value = Event(itemView.dealCardView.background)
                        itemView.dealCardView.findNavController().navigate(R.id.nav_deal, bundle)
                    }
                })
            }
            R.layout.fake_deal_layout ->
            {

            }
        }
        //println("**************view id = ${res} *********************")


    }

}


