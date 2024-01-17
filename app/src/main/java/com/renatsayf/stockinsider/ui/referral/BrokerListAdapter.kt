package com.renatsayf.stockinsider.ui.referral

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.ItemPartnerBinding
import com.renatsayf.stockinsider.ui.referral.models.Broker
import com.squareup.picasso.Picasso

class BrokerListAdapter private constructor(): ListAdapter<Broker, BrokerListAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Broker>() {
    override fun areItemsTheSame(oldItem: Broker, newItem: Broker): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Broker, newItem: Broker): Boolean {
        return oldItem.name == newItem.name
    }
}) {

    companion object {
        private var instance: BrokerListAdapter? = null

        fun getInstance(): BrokerListAdapter {

            return if (instance == null) {
                instance = BrokerListAdapter()
                instance!!
            }
            else {
                instance!!
            }
        }
    }

    private var listener: Listener? = null
    fun setOnItemClickListener(listener: Listener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPartnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val broker = currentList[position]
        holder.bind(broker)
    }


    inner class ViewHolder(private val binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(broker: Broker) {

            with(binding as ItemPartnerBinding) {

                val logoUri = Uri.parse(broker.logo)
                Picasso.get().load(logoUri)
                    .placeholder(R.drawable.image_area_chart_144dp)
                    .into(ivPartnerLogo)

                tvPartnerName.text = HtmlCompat.fromHtml(broker.name, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvHeader.text = broker.header
                tvDescription.text = broker.description
                btnOpenAccount.text = broker.buttonText
                btnOpenAccount.setBackgroundColor(Color.parseColor(broker.buttonColor))
                btnOpenAccount.setOnClickListener {
                    listener!!.onBrokerListItemClick(broker.reference)
                }
            }
        }
    }

    interface Listener {
        fun onBrokerListItemClick(reference: String)
    }

}