package com.renatsayf.stockinsider.ui.adapters

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import java.util.*
import kotlin.collections.ArrayList

class TickersListAdapter(
        context : Context, resource : Int, items : Array<String> = arrayOf()
                        ) : ArrayAdapter<String>(context, resource, items)
{
    internal var tempItems : MutableList<Any> = mutableListOf()
    internal var suggestions : MutableList<Any> = arrayListOf()

    init
    {
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

    override fun getFilter() : Filter
    {

        return filter
    }

    override fun getCount() : Int
    {
        return suggestions.size
    }


}