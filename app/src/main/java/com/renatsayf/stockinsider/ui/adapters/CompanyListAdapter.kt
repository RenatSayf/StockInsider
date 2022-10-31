package com.renatsayf.stockinsider.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.utils.setVisible


class CompanyListAdapter(private val listener: Listener? = null) : ListAdapter<Company, CompanyListAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Company>() {
    override fun areItemsTheSame(oldItem: Company, newItem: Company): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Company, newItem: Company): Boolean {
        return oldItem.ticker == newItem.ticker && oldItem.company == newItem.company
    }

}) {

    private var isDeletable = false

    fun setEnable(flag: Boolean) {
        isDeletable = flag
    }

    interface Listener {
        fun onItemClick(company: Company)
        fun onItemRemoved(company: Company)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TickerLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val company = currentList[position]
        holder.bind(company)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    val items: List<Company>
        get() {
            return this.currentList
        }



    inner class ViewHolder(private val binding: TickerLayoutBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(company: Company) {

            with(binding) {

                tickerLayout.children.forEach { view: View ->
                    if (view is TextView) {
                        view.apply {
                            textSize = 16.0f
                            setTextColor(Color.WHITE)
                        }
                    }
                }

                tickerTV.text = company.ticker
                companyNameTV.text = company.company

                tickerLayout.setOnClickListener {
                    listener?.onItemClick(company)
                }

                btnDelete.apply {
                    setVisible(isDeletable)
                    setOnClickListener {
                        listener?.onItemRemoved(company)
                    }
                }
            }
        }

    }

}
