@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TrackingItemBinding
import com.renatsayf.stockinsider.db.RoomSearchSet


class TrackingAdapter(
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
        fun onTrackingAdapterSwitcherOnChange(set: RoomSearchSet, isChecked: Boolean, position: Int)
        fun onTrackingAdapterVisibilityButtonClick(set: RoomSearchSet, position: Int)
        fun onTrackingAdapterInfoButtonClick(set: RoomSearchSet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TrackingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val set = getItem(position)
        holder.bind(set, position)
    }

    inner class ViewHolder(private val binding: TrackingItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(set: RoomSearchSet, position: Int) {
            with(binding) {
                trackerName.text = set.queryName
                dealType.text = set.ticker.ifEmpty { "ALL" }

                trackingSwitcher.isChecked = set.isTracked

                editButton.setOnClickListener {
                    listener?.onTrackingAdapterEditButtonClick(set, position)
                }

                deleteButton.setOnClickListener {
                    listener?.onTrackingAdapterDeleteButtonClick(set, position)
                }

                trackingSwitcher.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(p0: CompoundButton, p1: Boolean) {
                        listener?.onTrackingAdapterSwitcherOnChange(set, p1, position)
                    }
                })

                btnVisibility.setOnClickListener {
                    listener?.onTrackingAdapterVisibilityButtonClick(set, position)
                }

                btnInfo.setOnClickListener {
                    listener?.onTrackingAdapterInfoButtonClick(set)
                }
            }
        }
    }
}
