@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TrackingItemBinding
import com.renatsayf.stockinsider.db.RoomSearchSet


class TrackingAdapter(
    //private var list: MutableList<RoomSearchSet> = mutableListOf(),
    private val listener: Listener? = null
): ListAdapter<RoomSearchSet, TrackingAdapter.ViewHolder>(object : DiffUtil.ItemCallback<RoomSearchSet>() {
    override fun areItemsTheSame(oldItem: RoomSearchSet, newItem: RoomSearchSet): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: RoomSearchSet, newItem: RoomSearchSet): Boolean {
        return oldItem.id == newItem.id
    }

}) {

    interface Listener {
        fun onTrackingAdapterEditButtonClick(set: RoomSearchSet, position: Int)
        fun onTrackingAdapterDeleteButtonClick(set: RoomSearchSet, position: Int)
        fun onTrackingAdapterSwitcherOnChange(set: RoomSearchSet, checked: Boolean, position: Int)
        fun onTrackingAdapterVisibilityButtonClick(set: RoomSearchSet, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TrackingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val set = currentList[position]
        holder.bind(set, position)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

//    @SuppressLint("NotifyDataSetChanged")
//    fun setItems(list: MutableList<RoomSearchSet>) {
//        this.list = list
//        notifyDataSetChanged()
//    }
//
//    fun modifyItem(set: RoomSearchSet, position: Int) {
//        list[position] = set
//    }

    inner class ViewHolder(private val binding: TrackingItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(set: RoomSearchSet, position: Int) {

            with(binding) {
                val context = trackerName.context

                trackerName.text = set.queryName
                val dealType = if (set.isPurchase && !set.isSale) {
                    binding.layoutItem.setBackgroundColor(context.getColor(R.color.buy1000000))
                    context.getString(R.string.text_purchase)
                }
                else if (set.isSale && !set.isPurchase) {
                    binding.layoutItem.setBackgroundColor(context.getColor(R.color.sale1000000))
                    context.getString(R.string.text_sale)
                }
                else {
                    binding.layoutItem.setBackgroundColor(context.getColor(R.color.colorWhite))
                    "${context.getString(R.string.text_purchase)} / ${context.getString(R.string.text_sale)}"
                }

                binding.dealType.text = dealType

                if (set.isDefault) editButton.visibility = View.GONE
                if (set.isDefault) deleteButton.visibility = View.GONE

                trackingSwitcher.isChecked = set.isTracked

                editButton.setOnClickListener {
                    listener?.onTrackingAdapterEditButtonClick(set, position)
                }

                deleteButton.setOnClickListener {
                    listener?.onTrackingAdapterDeleteButtonClick(set, position)
                }

                trackingSwitcher.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                        listener?.onTrackingAdapterSwitcherOnChange(set, p1, position)
                    }
                })

                btnVisibility.setOnClickListener {
                    listener?.onTrackingAdapterVisibilityButtonClick(set, position)
                }
            }
        }

    }

}