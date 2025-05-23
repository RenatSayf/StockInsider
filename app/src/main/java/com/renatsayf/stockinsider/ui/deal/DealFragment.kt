@file:Suppress("RedundantSamConstructor", "ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.deal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentDealBinding
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.ad.admob.AdMobIds
import com.renatsayf.stockinsider.ui.ad.admob.AdMobViewModel
import com.renatsayf.stockinsider.ui.result.insider.InsiderTradingFragment
import com.renatsayf.stockinsider.ui.result.ticker.TradingByTickerFragment
import com.renatsayf.stockinsider.ui.settings.isAdsDisabled
import com.renatsayf.stockinsider.utils.getParcelableCompat
import com.renatsayf.stockinsider.utils.setPopUpMenu
import com.renatsayf.stockinsider.utils.setVisible
import com.renatsayf.stockinsider.utils.startBrowserSearch
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale


@AndroidEntryPoint
class DealFragment : Fragment() {
    private lateinit var binding: FragmentDealBinding

    companion object {
        val TAG = "${this::class.java.simpleName}.Tag"
        val ARG_DEAL = "${this::class.java.simpleName}.deal"
        val ARG_TITLE = "${this::class.java.simpleName}.title"

        private const val MARKET_WATCH_URL = "https://www.marketwatch.com/investing/stock/"
        private const val GOOGLE_SEARCH_URL = "https://www.google.com/search?q="
    }

    private val viewModel: DealViewModel by lazy {
        ViewModelProvider(this)[DealViewModel::class.java]
    }

    private val adMobVM: AdMobViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {

            if (!requireContext().isAdsDisabled) {
                adMobVM.loadInterstitialAd(AdMobIds.INTERSTITIAL_3)
            }
        }

        adMobVM.interstitialAd.observe(this) { result ->
            result.onSuccess { ad ->
                ad.show(requireActivity())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deal = arguments?.getParcelableCompat<Deal>(ARG_DEAL)
        if (savedInstanceState == null) {
            deal?.let { viewModel.setDeal(it) }
        }

        binding.chartImagView.setOnClickListener {
            val ticker = binding.tickerTV.text.toString().lowercase().trim()
            val url = "$MARKET_WATCH_URL$ticker"
            startBrowserSearch(url)
        }

        viewModel.deal.observe(viewLifecycleOwner) { value ->
            with(binding) {

                val uri = value?.tickerRefer?.toUri()
                Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.image_area_chart_144dp)
                    .into(binding.chartImagView, object : Callback {
                    override fun onSuccess() {
                        imgLoadProgBar.setVisible(false)
                    }

                    override fun onError(e: Exception?) {
                        imgLoadProgBar.setVisible(false)
                    }
                })

                value?.let { mainDealLayout.setBackgroundColor(it.color) }

                companyNameTV.text = value.company
                companyNameTV.setOnClickListener {
                    (it as TextView).setTextIsSelectable(true)
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
                                    val name = value.company?.replace(Regex("[:punct:]"), "")
                                    val url = "$GOOGLE_SEARCH_URL$name"
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
                    (it as TextView).setTextIsSelectable(true)
                    value?.let { d -> transitionToCompanyDeals(d) }
                }

                filingDateTV.text = value.filingDate
                filingDateTV.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, value?.filingDateRefer?.toUri())
                    activity?.startActivity(intent)
                }
                tradeDateTV.text = value.tradeDate
                insiderNameTV.text = value.insiderName
                insiderNameTV.setOnClickListener {

                    (it as TextView).setTextIsSelectable(true)
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
                                    val name = value.insiderName?.replace(Regex("[:punct:]"), "")
                                    val url = "$GOOGLE_SEARCH_URL$name"
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

        binding.layoutMarketWatch.setOnClickListener {
            val ticker = binding.tickerTV.text.toString().lowercase().trim()
            val url = "$MARKET_WATCH_URL$ticker"
            startBrowserSearch(url)
        }

        binding.btnOpenAccount.setOnClickListener {
            findNavController().navigate(R.id.referralFragment)
        }

        binding.toolBar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    R.id.company_deals -> {
                        viewModel.deal.value?.let {
                            transitionToCompanyDeals(it)
                        }
                    }
                    R.id.company_info -> {
                        @Suppress("RegExpDuplicateCharacterInClass")
                        val name = viewModel.deal.value?.company?.replace(Regex("[:punct:]"), "")
                        val url = "$GOOGLE_SEARCH_URL$name"
                        startBrowserSearch(url)
                    }
                    R.id.insider_deals -> {
                        viewModel.deal.value?.let {
                            transitionToInsiderDeals(it)
                        }
                    }
                    R.id.insider_info -> {
                        @Suppress("RegExpDuplicateCharacterInClass")
                        val name = viewModel.deal.value?.insiderName?.replace(Regex("[:punct:]"), "")
                        val url = "$GOOGLE_SEARCH_URL$name"
                        startBrowserSearch(url)
                    }
                    R.id.market_watch -> {
                        val ticker = binding.tickerTV.text.toString().lowercase().trim()
                        val url = "$MARKET_WATCH_URL$ticker"
                        startBrowserSearch(url)
                    }
                    R.id.start_trading -> {
                        findNavController().navigate(R.id.referralFragment)
                    }
                }
                return false
            }

        }, viewLifecycleOwner)

        binding.root.forEach { child ->
            if (child is TextView) {
                child.onFocusChangeListener = object : View.OnFocusChangeListener {
                    override fun onFocusChange(p0: View?, p1: Boolean) {
                        if (!p1) {
                            (p0 as TextView).setTextIsSelectable(false)
                        }
                    }
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()

        binding.toolBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })
    }

    private fun transitionToCompanyDeals(deal: Deal) {
        binding.includedProgress.loadProgressBar.setVisible(true)
        deal.ticker?.let { ticker ->

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
                putString(TradingByTickerFragment.ARG_TICKER, ticker)
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
