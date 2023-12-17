@file:Suppress("RedundantSamConstructor")

package com.renatsayf.stockinsider.ui.deal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentDealBinding
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.ui.ad.AdsId
import com.renatsayf.stockinsider.ui.ad.YandexAdsViewModel
import com.renatsayf.stockinsider.ui.ad.admob.AdMobIds
import com.renatsayf.stockinsider.ui.ad.admob.AdMobViewModel
import com.renatsayf.stockinsider.ui.main.NetInfoViewModel
import com.renatsayf.stockinsider.ui.result.insider.InsiderTradingFragment
import com.renatsayf.stockinsider.ui.result.ticker.TradingByTickerFragment
import com.renatsayf.stockinsider.utils.getParcelableCompat
import com.renatsayf.stockinsider.utils.setPopUpMenu
import com.renatsayf.stockinsider.utils.setVisible
import com.renatsayf.stockinsider.utils.startBrowserSearch
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
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

    private val netInfoVM by activityViewModels<NetInfoViewModel>()

    private val yandexAdVM: YandexAdsViewModel by viewModels()

    private val adMobVM: AdMobViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentDealBinding.bind(view)

        if (savedInstanceState == null) {

            netInfoVM.countryCode.observe(viewLifecycleOwner) { result ->
                result.onSuccess { code ->
                    if (FireBaseConfig.sanctionsList.contains(code)) {
                        yandexAdVM.loadInterstitialAd(adId = AdsId.INTERSTITIAL_3)
                    }
                    else {
                        adMobVM.loadInterstitialAd(AdMobIds.INTERSTITIAL_3)
                    }
                }
            }
        }

        yandexAdVM.interstitialAd.observe(viewLifecycleOwner) { result ->
            result.onSuccess { ad ->
                ad.show(requireActivity())
            }
        }

        adMobVM.interstitialAd.observe(viewLifecycleOwner) { result ->
            result.onSuccess { ad ->
                ad.show(requireActivity())
            }
        }

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

                val uri = Uri.parse(value?.tickerRefer)
                Picasso.get().load(uri).into(binding.chartImagView, object : Callback {
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
                                    val name = value.insiderName?.replace(Regex("[:punct:]"), "")
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

        binding.layoutMarketWatch.setOnClickListener {
            val ticker = binding.tickerTV.text.toString().lowercase().trim()
            val url = "$MARKET_WATCH_URL$ticker"
            startBrowserSearch(url)
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
