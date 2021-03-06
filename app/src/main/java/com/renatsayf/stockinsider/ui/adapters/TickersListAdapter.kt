package com.renatsayf.stockinsider.ui.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.Companies
import kotlinx.android.synthetic.main.ticker_layout.view.*
import java.util.*
import kotlin.collections.ArrayList

class TickersListAdapter(
        context : Activity, items : Array<Companies> = arrayOf()
                        ) : ArrayAdapter<Any>(context, R.layout.ticker_layout), Filterable
{
    private var _context : Context? = null

    internal var tempItems : MutableList<Companies> = mutableListOf()
    internal var suggestions : MutableList<Companies> = mutableListOf()

    init
    {
        _context = context
        tempItems = items.toMutableList()
        suggestions = ArrayList()
    }

    private var filter : Filter = object : Filter()
    {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun performFiltering(constraint : CharSequence?) : FilterResults
        {
            return if (constraint != null)
            {
                val listConstraint = constraint.split(" ").toMutableList()
                val lastConstraint = listConstraint[listConstraint.size - 1]
                suggestions.clear()

                tempItems.forEach {company ->
                    val isContains = company.ticker.toLowerCase(Locale.getDefault()).contains(lastConstraint.toLowerCase(Locale.getDefault()))
                    if (isContains)
                    {
                        suggestions.add(company)
                    }
                }

                for (ticker in listConstraint)
                {
                    suggestions.removeIf{company ->
                        company.ticker == ticker
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
                suggestions = results.values as MutableList<Companies>
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

    @SuppressLint("ViewHolder")
    override fun getView(position : Int, convertView : View?, parent : ViewGroup) : View
    {
        val inflater= LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.ticker_layout, parent, false) as LinearLayout
        layout.tickerTV.text = suggestions[position].ticker
        layout.companyNameTV.text = suggestions[position].company
        return layout
    }


}