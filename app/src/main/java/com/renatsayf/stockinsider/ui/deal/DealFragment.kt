@file:Suppress("RedundantSamConstructor")

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
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentDealBinding
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.result.insider.InsiderTradingFragment
import com.renatsayf.stockinsider.ui.result.ticker.TradingByTickerFragment
import com.renatsayf.stockinsider.utils.getParcelableCompat
import com.renatsayf.stockinsider.utils.setPopUpMenu
import com.renatsayf.stockinsider.utils.setVisible
import com.renatsayf.stockinsider.utils.startBrowserSearch
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*


@AndroidEntryPoint
class DealFragment : Fragment(R.layout.fragment_deal) {
    private lateinit var binding: FragmentDealBinding

    companion object {
        val TAG = "${this::class.java.simpleName}.Tag"
        val ARG_DEAL = "${this::class.java.simpleName}.deal"
        val ARG_TITLE = "${this::class.java.simpleName}.title"

        private const val MARKET_WATCH_URL = "https://www.marketwatch.com/investing/stock/"
    }

    private val viewModel: DealViewModel by lazy {
        ViewModelProvider(this)[DealViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentDealBinding.bind(view)

        val deal = arguments?.getParcelableCompat<Deal>(ARG_DEAL)
        if (savedInstanceState == null) {
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
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.imgLoadProgBar.setVisible(false)
                            return true
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            resource?.let { viewModel.setChart(it) }
                            binding.imgLoadProgBar.setVisible(false)
                            return true
                        }
                    }).into(binding.chartImagView)

                value?.let { mainDealLayout.setBackgroundColor(it.color) }

                companyNameTV.text = value.company
                companyNameTV.setOnClickListener {
                    it.setPopUpMenu(R.menu.company_deals_menu).apply {
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.show_deals -> {
                                    value?.let { d -> transitionToCompanyDeals(d) }
                                    dismiss()
                                }
                                R.id.search_info -> {
                                    @Suppress(
                                        "RegExpRedundantNestedCharacterClass",
                                        "RegExpDuplicateCharacterInClass"
                                    )
                                    val name = value.company?.replace(Regex("[[:punct:]]"), "")
                                    val url = "https://www.google.com/search?q=$name"
                                    startBrowserSearch(url)
                                    dismiss()
                                }
                                R.id.market_watch -> {
                                    val ticker = binding.tickerTV.text.toString().lowercase().trim()
                                    val url = "$MARKET_WATCH_URL$ticker"
                                    startBrowserSearch(url)
                                }
                            }
                            true
                        }
                    }.show()
                }
                tickerTV.text = value.ticker
                tickerTV.setOnClickListener {
                    value?.let { d -> transitionToCompanyDeals(d) }
                }

                filingDateTV.text = value.filingDate
                filingDateTV.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(value?.filingDateRefer))
                    activity?.startActivity(intent)
                }
                tradeDateTV.text = value.tradeDate
                insiderNameTV.text = value.insiderName
                insiderNameTV.setOnClickListener {

                    it.setPopUpMenu(R.menu.insider_deals_menu).apply {
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.show_deals -> {
                                    transitionToInsiderDeals(value)
                                    dismiss()
                                }
                                R.id.search_info -> {

                                    @Suppress(
                                        "RegExpRedundantNestedCharacterClass",
                                        "RegExpDuplicateCharacterInClass"
                                    )
                                    val name = value.insiderName?.replace(Regex("[[:punct:]]"), "")
                                    val url = "https://www.google.com/search?q=$name"
                                    startBrowserSearch(url)
                                    dismiss()
                                }
                            }
                            true
                        }
                    }.show()

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

        binding.layoutMarketWatch.setOnClickListener {
            val ticker = binding.tickerTV.text.toString().lowercase().trim()
            val url = "$MARKET_WATCH_URL$ticker"
            startBrowserSearch(url)
        }

        binding.toolBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun transitionToCompanyDeals(deal: Deal) {
        binding.includedProgress.loadProgressBar.setVisible(true)
        deal.ticker?.let { t ->

            findNavController().navigate(R.id.nav_trading_by_ticker, Bundle().apply {
                putString(
                    TradingByTickerFragment.ARG_TOOL_BAR_TITLE,
                    getString(R.string.text_trading_by_company)
                )
                putString(TradingByTickerFragment.ARG_TITLE, getString(R.string.text_company))
                putString(
                    TradingByTickerFragment.ARG_COMPANY_NAME,
                    binding.companyNameTV.text.toString()
                )
                putString(TradingByTickerFragment.ARG_TICKER, t)
            })
        }
    }

    private fun transitionToInsiderDeals(deal: Deal) {
        binding.includedProgress.loadProgressBar.setVisible(true)
        val insiderNameRefer = deal.insiderNameRefer

        findNavController().navigate(R.id.nav_insider_trading, Bundle().apply {
            putString(
                InsiderTradingFragment.ARG_TOOL_BAR_TITLE,
                getString(R.string.text_insider_deals)
            )
            putString(InsiderTradingFragment.ARG_TITLE, getString(R.string.text_insider))
            putString(InsiderTradingFragment.ARG_INSIDER_NAME, insiderNameRefer)
        })
    }

}
