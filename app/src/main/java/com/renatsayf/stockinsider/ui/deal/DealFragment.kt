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
import androidx.navigation.findNavController
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

        val deal = arguments?.get(ARG_DEAL) as Deal

        if (savedInstanceState == null)
        {
            val title = arguments?.getString(ARG_TITLE)
            if (!title.isNullOrEmpty()) {
                (activity as MainActivity).supportActionBar?.title = title
            }

            viewModel.setDeal(deal)
        }

        binding.chartImagView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal.tickerRefer))
            activity?.startActivity(intent)
        }

        viewModel.deal.observe(viewLifecycleOwner) { d ->
            with(binding) {

                val uri = Uri.parse(deal.tickerRefer)
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

                mainDealLayout.setBackgroundColor(deal.color)

                companyNameTV.text = d.company
                companyNameTV.setOnClickListener {
                    companyNameOnClick(deal)
                }
                tickerTV.text = d.ticker
                tickerTV.setOnClickListener {
                    companyNameOnClick(deal)
                }
                filingDateTV.text = d.filingDate
                filingDateTV.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal.filingDateRefer))
                    activity?.startActivity(intent)
                }
                tradeDateTV.text = d.tradeDate
                insiderNameTV.text = d.insiderName
                insiderNameTV.setOnClickListener {
                    transitionToInsiderDeals(d)
                }

                insiderTitleTV.text = d.insiderTitle
                tradeTypeTV.text = d.tradeType
                priceTV.text = d.price
                qtyTV.text = d.qty.toString()
                ownedTV.text = d.owned
                deltaOwnTV.text = d.deltaOwn
                valueTV.text = NumberFormat.getInstance(Locale.getDefault()).format(d.volume)
            }
        }

        viewModel.chart.observe(viewLifecycleOwner) {
            binding.chartImagView.setImageDrawable(it)
        }

    }

    private fun companyNameOnClick(deal: Deal) {
        binding.includedProgress.loadProgressBar.visibility = View.VISIBLE
        deal.ticker?.let {t ->
            viewModel.getTradingByTicker(t).observe(viewLifecycleOwner) { list ->
                binding.includedProgress.loadProgressBar.visibility = View.GONE
                if (list.isNotEmpty()) {
                    val bundle = Bundle().apply {
                        putParcelableArrayList(TradingByTickerFragment.ARG_TICKER_DEALS, list)
                        putString(TradingByTickerFragment.ARG_TITLE, getString(R.string.text_company))
                        putString(TradingByTickerFragment.ARG_COMPANY_NAME, binding.companyNameTV.text.toString())
                    }
                    (activity as MainActivity).findNavController(R.id.nav_host_fragment).navigate(R.id.nav_trading_by_ticker, bundle)
                }
            }
        }
    }

    private fun transitionToInsiderDeals(deal: Deal) {
        binding.includedProgress.loadProgressBar.visibility = View.VISIBLE
        val insiderNameRefer = deal.insiderNameRefer

        insiderNameRefer?.let { name ->
            viewModel.getInsiderDeals(name).observe(viewLifecycleOwner) { list ->
                binding.includedProgress.loadProgressBar.visibility = View.GONE
                if (list.isNotEmpty()) {
                    val bundle = Bundle().apply {
                        putString(InsiderTradingFragment.ARG_TITLE, getString(R.string.text_insider))
                        putString(InsiderTradingFragment.ARG_INSIDER_NAME, deal.insiderName)
                        putParcelableArrayList(InsiderTradingFragment.ARG_INSIDER_DEALS, list)
                    }
                    (activity as MainActivity).findNavController(R.id.nav_host_fragment).navigate(R.id.nav_insider_trading, bundle)
                }
            }
        }
    }

}
