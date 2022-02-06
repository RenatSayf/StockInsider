package com.renatsayf.stockinsider.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.db.Companies



class CompanyListAdapter(private var list: MutableList<Companies>,
                            private val listener: Listener? = null
) : RecyclerView.Adapter<CompanyListAdapter.ViewHolder>() {

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

    inner class ViewHolder(private val binding: TickerLayoutBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(company: Companies, position: Int) {
            binding.tickerTV.text = company.ticker
            binding.companyNameTV.text = company.company

            binding.tickerLayout.setOnClickListener {
                listener?.onItemClick(company)
            }
        }

    }

}
