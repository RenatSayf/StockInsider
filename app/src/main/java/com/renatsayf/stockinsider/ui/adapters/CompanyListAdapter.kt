package com.renatsayf.stockinsider.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.db.Companies



class CompanyListAdapter(private val listener: Listener? = null) : RecyclerView.Adapter<CompanyListAdapter.ViewHolder>() {

    private val list = mutableListOf<Companies>()

    interface Listener {
        fun onItemClick(company: Companies)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TickerLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val company = list[position]
        holder.bind(company, position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addItems(list: List<Companies>) {
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: TickerLayoutBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(company: Companies, position: Int) {

            with(binding) {

                tickerLayout.children.forEach { view: View ->
                    (view as TextView).apply {
                        textSize = 16.0f
                        setTextColor(Color.WHITE)
                    }
                }

                tickerTV.text = company.ticker
                companyNameTV.text = company.company

                tickerLayout.setOnClickListener {
                    listener?.onItemClick(company)
                }
            }
        }

    }

}
