package com.renatsayf.stockinsider.ui.deal

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.utils.AppLog
import kotlinx.android.synthetic.main.fragment_deal.*
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

class DealFragment : Fragment()
{
    companion object
    {
        val TAG = "${this::class.java.simpleName}.deal_fragment"
        val ARG_DEAL = "${this::class.java.simpleName}.deal"
    }

    @Inject
    lateinit var appLog: AppLog

    private lateinit var viewModel : DealViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        MainActivity.appComponent.inject(this)
    }

    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View?
    {
        appLog.print(TAG, "**********  onCreateView()  ***********")
        return inflater.inflate(R.layout.fragment_deal, container, false)
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DealViewModel::class.java)

        viewModel.background.observe(viewLifecycleOwner, {
            it?.let { mainDealLayout.background = it }
        })

        val deal = arguments?.get(ARG_DEAL) as Deal
        deal.let{ d ->
            DealListAdapter.dealAdapterItemClick.observe(viewLifecycleOwner, { event ->
                if (!event.hasBeenHandled)
                {
                    event.getContent()?.let {
                        viewModel.setLayOutBackground(it)
                        mainDealLayout.background = it
                    }

                    companyNameTV.text = d.company
                    companyNameTV.setOnClickListener {
                        //println(d.companyRefer.toString())
                    }
                    tickerTV.text = d.ticker
                    tickerTV.setOnClickListener {
                        //println(d.tickerRefer.toString())
                    }
                    filingDateTV.text = d.filingDate
                    filingDateTV.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal.filingDateRefer))
                        activity?.startActivity(intent)
                    }
                    tradeDateTV.text = d.tradeDate
                    insiderNameTV.text = d.insiderName
                    insiderNameTV.setOnClickListener {
                        //println(d.insiderNameRefer.toString())
                    }
                    insiderTitleTV.text = d.insiderTitle
                    tradeTypeTV.text = d.tradeType
                    priceTV.text = d.price
                    qtyTV.text = d.qty.toString()
                    ownedTV.text = d.owned
                    deltaOwnTV.text = d.deltaOwn
                    valueTV.text = NumberFormat.getInstance(Locale.getDefault()).format(d.volume)

                    val uri = Uri.parse(deal.tickerRefer)
                    Glide.with(this).load(uri)
                        .listener(object : RequestListener<Drawable>{
                            override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                            ): Boolean
                            {
                                e?.message?.let { appLog.print(TAG, it) }
                                imgLoadProgBar.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                            ): Boolean
                            {
                                appLog.print(TAG, "**********  Chart is loaded  ***********")
                                imgLoadProgBar.visibility = View.GONE
                                return false
                            }
                        })
                        .into(chartImagView)

                    chartImagView.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal.tickerRefer))
                        activity?.startActivity(intent)
                    }
                }
                else
                {
                    onDetach()
                }
            })


        }

//        requireActivity().onBackPressedDispatcher.addCallback(this){
//            mainDealLayout.findNavController().currentBackStackEntry
//        }
    }

}
