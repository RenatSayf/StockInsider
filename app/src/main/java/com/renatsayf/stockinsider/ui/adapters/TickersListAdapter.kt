package com.renatsayf.stockinsider.ui.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import com.renatsayf.stockinsider.R
import kotlinx.android.synthetic.main.ticker_layout.view.*
import java.util.*
import kotlin.collections.ArrayList

class TickersListAdapter(
        context : Activity, items : Array<String> = arrayOf()
                        ) : ArrayAdapter<String>(context, R.layout.ticker_layout), Filterable
{
    internal var _context : Context? = null

    internal var tempItems : MutableList<Any> = mutableListOf()
    internal var suggestions : MutableList<Any> = arrayListOf()

    init
    {
        _context = context
        tempItems = items.toMutableList()
        suggestions = ArrayList()
    }

    private var filter : Filter = object : Filter()
    {

        override fun performFiltering(constraint : CharSequence?) : FilterResults
        {
            return if (constraint != null)
            {
                val listConstraint = constraint.split(" ")
                val lastConstraint = listConstraint[listConstraint.size - 1]
                suggestions.clear()

                tempItems.forEach {
                    val isContains = it.toString().toLowerCase(Locale.getDefault()).contains(lastConstraint.toLowerCase(Locale.getDefault()))
                    if (isContains)
                    {
                        suggestions.add(it)
                    }
                }

                listConstraint.forEach {
                    if (suggestions.contains(it))
                    {
                        suggestions.remove(it)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = suggestions
                filterResults.count = suggestions.size
                filterResults
            }
            else
            {
                FilterResults()
            }
        }

        override fun publishResults(constraint : CharSequence?, results : FilterResults)
        {
            val values = results.values
            values?.let {
                suggestions = results.values as MutableList<Any>
                notifyDataSetChanged()
            }
        }
    }

    override fun getItem(position : Int) : String?
    {
        return suggestions[position].toString()
    }

    override fun getItemId(position : Int) : Long
    {
        return position.toLong()
    }

    override fun getFilter() : Filter
    {

        return filter
    }

    override fun getCount() : Int
    {
        return suggestions.size
    }

    override fun getView(position : Int, convertView : View?, parent : ViewGroup) : View
    {
        val inflater= LayoutInflater.from(context)
        val convertView = inflater.inflate(R.layout.ticker_layout, parent, false) as LinearLayout
        convertView.tickerTV.text = suggestions[position].toString()
        convertView.companyNameTV.text = "Microsoft Corp"
        return convertView
    }


}