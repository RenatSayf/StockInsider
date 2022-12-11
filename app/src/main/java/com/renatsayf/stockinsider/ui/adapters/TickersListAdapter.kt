package com.renatsayf.stockinsider.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.Company
import java.util.*



class TickersListAdapter(
    context: Context,
    items: Array<Company> = arrayOf()
) : ArrayAdapter<Any>(context, R.layout.ticker_layout), Filterable {

    private var tempItems: MutableList<Company> = mutableListOf()
    private var suggestions: MutableList<Company> = mutableListOf()

    init {
        tempItems = items.toMutableList()
    }

    private var filter: Filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return if (constraint != null) {
                val listConstraint = constraint.split(" ").toMutableList()
                val lastConstraint = listConstraint[listConstraint.size - 1]
                suggestions.clear()

                tempItems.forEach { company ->
                    val isContains = company.ticker.lowercase(Locale.getDefault())
                        .contains(lastConstraint.lowercase(Locale.getDefault()))
                    if (isContains) {
                        suggestions.add(company)
                    }
                }

                for (ticker in listConstraint) {
                    suggestions.removeIf { company ->
                        company.ticker == ticker
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

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            val values = results.values
            values?.let {
                suggestions = results.values as MutableList<Company>
                notifyDataSetChanged()
            }
        }
    }

    override fun getItem(position: Int): String {
        return suggestions[position].toString()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getFilter(): Filter {
        return filter
    }

    override fun getCount(): Int {
        return suggestions.size
    }

    fun addItems(list: List<Company>) {
        tempItems = list.toMutableList()
        notifyDataSetChanged()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.ticker_layout, parent, false) as LinearLayout
        layout.findViewById<TextView>(R.id.tickerTV).text = suggestions[position].ticker
        layout.findViewById<TextView>(R.id.companyNameTV).text = suggestions[position].company
        return layout
    }


}