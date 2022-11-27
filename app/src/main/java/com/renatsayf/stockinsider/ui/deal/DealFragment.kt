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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentDealBinding
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.result.insider.InsiderTradingFragment
import com.renatsayf.stockinsider.ui.result.ticker.TradingByTickerFragment
import com.renatsayf.stockinsider.utils.getParcelableCompat
import com.renatsayf.stockinsider.utils.setVisible
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*


@AndroidEntryPoint
class DealFragment : Fragment(R.layout.fragment_deal)
{
    private lateinit var binding: FragmentDealBinding

    companion object
    {
        val TAG = "${this::class.java.simpleName}.Tag"
        val ARG_DEAL = "${this::class.java.simpleName}.deal"
        val ARG_TITLE = "${this::class.java.simpleName}.title"
    }

    private lateinit var viewModel : DealViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[DealViewModel::class.java]
    }

    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View?
    {
        return inflater.inflate(R.layout.fragment_deal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentDealBinding.bind(view)

        val deal = arguments?.getParcelableCompat<Deal>(ARG_DEAL)

        if (savedInstanceState == null)
        {
            val title = arguments?.getString(ARG_TITLE)
            if (!title.isNullOrEmpty()) {
                (activity as MainActivity).supportActionBar?.title = title
            }

            deal?.let { viewModel.setDeal(it) }
        }

        binding.chartImagView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal?.tickerRefer))
            activity?.startActivity(intent)
        }

        viewModel.deal.observe(viewLifecycleOwner) { value ->
            with(binding) {

                val uri = Uri.parse(value?.tickerRefer)
                Glide.with(this@DealFragment).load(uri)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            binding.imgLoadProgBar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            resource?.let { viewModel.setChart(it) }
                            binding.imgLoadProgBar.visibility = View.GONE
                            return false
                        }
                    }).into(binding.chartImagView)

                value?.let { mainDealLayout.setBackgroundColor(it.color) }

                companyNameTV.text = value.company
                companyNameTV.setOnClickListener {
                    value?.let { d -> companyNameOnClick(d) }
                }
                tickerTV.text = value.ticker
                tickerTV.setOnClickListener {
                    value?.let { d -> companyNameOnClick(d) }
                }
                filingDateTV.text = value.filingDate
                filingDateTV.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(value?.filingDateRefer))
                    activity?.startActivity(intent)
                }
                tradeDateTV.text = value.tradeDate
                insiderNameTV.text = value.insiderName
                insiderNameTV.setOnClickListener {
                    transitionToInsiderDeals(value)
                }

                insiderTitleTV.text = value.insiderTitle
                tradeTypeTV.text = value.tradeType
                priceTV.text = value.price
                qtyTV.text = value.qty.toString()
                ownedTV.text = value.owned
                deltaOwnTV.text = value.deltaOwn
                valueTV.text = NumberFormat.getInstance(Locale.getDefault()).format(value.volume)
            }
        }

        viewModel.chart.observe(viewLifecycleOwner) {
            binding.chartImagView.setImageDrawable(it)
        }

    }

    private fun companyNameOnClick(deal: Deal) {
        binding.includedProgress.loadProgressBar.setVisible(true)
        deal.ticker?.let { t ->

            findNavController().navigate(R.id.nav_trading_by_ticker, Bundle().apply {
                putString(TradingByTickerFragment.ARG_TITLE, getString(R.string.text_company))
                putString(TradingByTickerFragment.ARG_COMPANY_NAME, binding.companyNameTV.text.toString())
                putString(TradingByTickerFragment.ARG_TICKER, t)
            })
        }
    }

    private fun transitionToInsiderDeals(deal: Deal) {
        binding.includedProgress.loadProgressBar.setVisible(true)
        val insiderNameRefer = deal.insiderNameRefer

        findNavController().navigate(R.id.nav_insider_trading, Bundle().apply {
            putString(InsiderTradingFragment.ARG_TITLE, getString(R.string.text_insider))
            putString(InsiderTradingFragment.ARG_INSIDER_NAME, insiderNameRefer)
        })


    }

}
