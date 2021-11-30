package com.renatsayf.stockinsider.ui.deal

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.motion.widget.MotionLayout
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
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.result.insider.InsiderTradingFragment
import com.renatsayf.stockinsider.ui.result.ticker.TradingByTickerFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.text.NumberFormat
import java.util.*



class DealFragment : Fragment(R.layout.fragment_deal)
{
    private lateinit var binding: FragmentDealBinding

    companion object
    {
        val TAG = "${this::class.java.simpleName}.Tag"
        val ARG_DEAL = "${this::class.java.simpleName}.deal"
    }

    private lateinit var viewModel : DealViewModel
    private var composite = CompositeDisposable()

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

        viewModel.background.observe(viewLifecycleOwner, {
            it?.let { binding.mainDealLayout.background = it }
        })

        val deal = arguments?.get(ARG_DEAL) as Deal
        viewModel.setDeal(deal)
        deal.let{
            DealListAdapter.dealAdapterItemClick.observe(viewLifecycleOwner, { event ->
                if (!event.hasBeenHandled)
                {
                    event.getContent()?.let {
                        viewModel.setLayOutBackground(it)
                        binding.mainDealLayout.background = it
                    }

                    val uri = Uri.parse(deal.tickerRefer)
                    Glide.with(this).load(uri)
                        .listener(object : RequestListener<Drawable>{
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean
                            {
                                binding.imgLoadProgBar.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean
                            {
                                resource?.let { viewModel.setChart(it) }
                                binding.imgLoadProgBar.visibility = View.GONE
                                return false
                            }
                        })
                        .into(binding.chartImagView)
                }
            })
        }

        binding.chartAnimView.clickMotionLayout.apply {
            setOnClickListener {
                binding.chartAnimView.clickMotionLayout.transitionToEnd()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal.tickerRefer))
                activity?.startActivity(intent)
            }
        }

        val loadProgressBar = (activity as MainActivity).findViewById<ProgressBar>(R.id.loadProgreesBar)

        binding.insiderNameMotionLayout.apply {
            setOnClickListener {
                this.transitionToEnd()
            }
            setTransitionListener(object : MotionLayout.TransitionListener{
                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int)
                {}

                override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float)
                {}

                override fun onTransitionCompleted(layout: MotionLayout?, p1: Int)
                {
                    loadProgressBar.visibility = View.VISIBLE
                    val insiderNameRefer = deal.insiderNameRefer
                    insiderNameRefer?.let { name ->
                        val subscribe = viewModel.getInsiderTrading(name)
                            .subscribe({ list ->
                                loadProgressBar.visibility = View.GONE
                                           val bundle = Bundle().apply {
                                               putString(InsiderTradingFragment.ARG_TITLE, getString(R.string.text_insider))
                                               putString(InsiderTradingFragment.ARG_INSIDER_NAME, deal.insiderName)
                                               putParcelableArrayList(
                                                       InsiderTradingFragment.ARG_INSIDER_DEALS,
                                                       list
                                               )
                                           }
                                           layout?.findNavController()?.navigate(R.id.nav_insider_trading, bundle)
                                       },
                                       { err ->
                                           err.printStackTrace()
                                           loadProgressBar.visibility = View.GONE
                                       })
                        composite.add(subscribe)
                    }
                }

                override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float)
                {}

            })
        }

        binding.tickerAnimView.clickMotionLayout.apply {
            setOnClickListener {
                this.transitionToEnd()
            }
            setTransitionListener(object : MotionLayout.TransitionListener{
                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int)
                {}

                override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float)
                {}

                override fun onTransitionCompleted(layout: MotionLayout?, p1: Int)
                {
                    clickAnimationCompleted(deal, layout)
                }

                override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float)
                {}

            })
        }

        binding.companyAnimView.clickMotionLayout.apply {
            setOnClickListener {
                this.transitionToEnd()
            }
            setTransitionListener(object : MotionLayout.TransitionListener{
                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int)
                {}

                override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float)
                {}

                override fun onTransitionCompleted(layout: MotionLayout?, p1: Int)
                {
                    clickAnimationCompleted(deal, layout)
                }

                override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float)
                {}

            })
        }

        viewModel.deal.observe(viewLifecycleOwner, { d ->
            with(binding) {
                companyNameTV.text = d.company
                companyNameTV.setOnClickListener {
                    companyAnimView.clickMotionLayout.transitionToEnd()
                }
                tickerTV.text = d.ticker
                tickerTV.setOnClickListener {
                    tickerAnimView.clickMotionLayout.transitionToEnd()
                }
                filingDateTV.text = d.filingDate
                filingDateTV.setOnClickListener {
                    filingDateAnimView.clickMotionLayout.transitionToEnd()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deal.filingDateRefer))
                    activity?.startActivity(intent)
                }
                tradeDateTV.text = d.tradeDate
                insiderNameTV.text = d.insiderName
                insiderNameTV.setOnClickListener {
                    insiderNameMotionLayout.transitionToEnd()
                }

                insiderTitleTV.text = d.insiderTitle
                tradeTypeTV.text = d.tradeType
                priceTV.text = d.price
                qtyTV.text = d.qty.toString()
                ownedTV.text = d.owned
                deltaOwnTV.text = d.deltaOwn
                valueTV.text = NumberFormat.getInstance(Locale.getDefault()).format(d.volume)
            }
        })

        viewModel.chart.observe(viewLifecycleOwner, {
            binding.chartImagView.setImageDrawable(it)
        })

    }

    private fun clickAnimationCompleted(deal: Deal, layout: MotionLayout?)
    {
        val loadProgressBar = (activity as MainActivity).findViewById<ProgressBar>(R.id.loadProgreesBar)

        loadProgressBar.visibility = View.VISIBLE
        deal.ticker?.let {t ->
            viewModel.getTradingByTicker(t)
                .subscribe({ list ->
                    loadProgressBar.visibility = View.GONE
                               if (list.size > 0)
                               {
                                   val bundle = Bundle().apply {
                                       putParcelableArrayList(TradingByTickerFragment.ARG_TICKER_DEALS, list)
                                       putString(TradingByTickerFragment.ARG_TITLE, getString(R.string.text_company))
                                       putString(TradingByTickerFragment.ARG_COMPANY_NAME, binding.companyNameTV.text.toString())
                                   }
                                   layout?.findNavController()?.navigate(R.id.nav_trading_by_ticker, bundle)
                               }
                           },
                           { err ->
                               err.printStackTrace()
                               loadProgressBar.visibility = View.GONE
                           })
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        composite.dispose()
    }

}
