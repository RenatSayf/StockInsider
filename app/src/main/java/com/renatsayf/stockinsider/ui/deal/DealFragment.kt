package com.renatsayf.stockinsider.ui.deal

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Deal
import kotlinx.android.synthetic.main.fragment_deal.*
import java.net.URI
import java.text.NumberFormat
import java.util.*

class DealFragment : Fragment()
{

    companion object
    {
        val TAG = "${this::class.java.simpleName}.deal_fragment"
    }

    private lateinit var viewModel : DealViewModel

    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View?
    {
        return inflater.inflate(R.layout.fragment_deal, container, false)
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DealViewModel::class.java)
        val deal = arguments?.get(TAG) as Deal
        deal.let{ d ->
            companyNameTV.text = d.company
            companyNameTV.setOnClickListener {
                println(d.companyRefer.toString())
            }
            tickerTV.text = d.ticker
            tickerTV.setOnClickListener {
                println(d.tickerRefer.toString())
            }
            filingDateTV.text = d.filingDate
            filingDateTV.setOnClickListener {
                println(d.filingDateRefer.toString())
            }
            tradeDateTV.text = d.tradeDate
            insiderNameTV.text = d.insiderName
            insiderNameTV.setOnClickListener {
                println(d.insiderNameRefer.toString())
            }
            insiderTitleTV.text = d.insiderTitle
            tradeTypeTV.text = d.tradeType
            priceTV.text = d.price
            qtyTV.text = d.qty.toString()
            ownedTV.text = d.owned
            deltaOwnTV.text = d.deltaOwn
            valueTV.text = NumberFormat.getInstance(Locale.getDefault()).format(d.volume)

            val uri = Uri.parse(deal.tickerRefer)
            Glide.with(this).load(uri).into(chartImagView)

        }
    }

}
