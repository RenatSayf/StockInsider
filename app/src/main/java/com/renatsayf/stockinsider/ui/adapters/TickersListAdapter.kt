package com.renatsayf.stockinsider.ui.adapters

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import java.util.*
import kotlin.collections.ArrayList

class TickersListAdapter(
        context : Context,
        resource : Int,
        items: Array<String> = arrayOf()
                        ) : ArrayAdapter<String>(context, resource, items)
{
    private var filter: Filter = object : Filter()
    {

        internal var tempItems: MutableList<Any> = mutableListOf()
        internal var suggestions: MutableList<Any> = mutableListOf()

        init {
            tempItems = items.toMutableList()
            suggestions = ArrayList()
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return if (constraint != null) {
                suggestions.clear()
                tempItems.forEach {
                    if (it.toString().toLowerCase(Locale.getDefault()).contains(constraint.toString().toLowerCase(Locale.getDefault()))) {
                        suggestions.add(it)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = suggestions
                filterResults.count = suggestions.size
                filterResults
            } else {
                FilterResults()
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            val filterList = results.values as? ArrayList<String>
            if (results.count > 0) {
                filterList?.clear()
                filterList?.forEach {
                    add(it)
                }.also {
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getFilter() : Filter
    {
        return filter
    }
}