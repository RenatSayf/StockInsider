@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TrackingItemBinding
import com.renatsayf.stockinsider.db.RoomSearchSet

class TrackingAdapter(private val list: List<RoomSearchSet>,
                        private val listener: Listener? = null): RecyclerView.Adapter<TrackingAdapter.ViewHolder>() {

    interface Listener {
        fun onTrackingAdapterEditButtonClick(set: RoomSearchSet)
        fun onTrackingAdapterDeleteButtonClick(set: RoomSearchSet)
        fun onTrackingAdapterSwitcherOnChange(set: RoomSearchSet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TrackingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sets = list[position]
        holder.bind(sets)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(private val binding: TrackingItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(set: RoomSearchSet) {

            val context = binding.trackerName.context

            binding.trackerName.text = set.queryName
            val dealType = if (set.isPurchase && !set.isSale) {
                binding.root.setCardBackgroundColor(context.getColor(R.color.buy1000000))
                context.getString(R.string.text_purchase)
            }
            else if (set.isSale && !set.isPurchase) {
                binding.root.setBackgroundColor(context.getColor(R.color.sale1000000))
                context.getString(R.string.text_sale)
            }
            else "${context.getString(R.string.text_purchase)} / ${context.getString(R.string.text_sale)}"

            binding.dealType.text = dealType

            if (set.isDefault) binding.editButton.visibility = View.GONE
            if (set.isDefault) binding.deleteButton.visibility = View.GONE
            binding.trackingSwitcher.isChecked = set.isTracked

                binding.editButton.setOnClickListener {
                listener?.onTrackingAdapterEditButtonClick(set)
            }

            binding.deleteButton.setOnClickListener {
                listener?.onTrackingAdapterDeleteButtonClick(set)
            }

            binding.trackingSwitcher.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    listener?.onTrackingAdapterSwitcherOnChange(set)
                }
            })
        }

    }

}