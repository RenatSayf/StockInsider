@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentResultBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.IDeal
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.deal.DealFragment
import com.renatsayf.stockinsider.ui.dialogs.SaveSearchDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.utils.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ResultFragment : Fragment(R.layout.fragment_result), DealListAdapter.Listener, SaveSearchDialog.Listener {

    companion object
    {
        val TAG = "${this::class.java.simpleName}.TAG"
        val ARG_SEARCH_SET = "${this::class.java.simpleName}.ARG_SEARCH_SET"
        val ARG_TITLE = "${this::class.java.simpleName}.ARG_TITLE"
    }

    private lateinit var binding: FragmentResultBinding
    private val resultVM : ResultViewModel by viewModels()
    private val mainViewModel : MainViewModel by viewModels()
    private var roomSearchSet: RoomSearchSet? = null

    private val dealsAdapter: DealListAdapter by lazy {
        DealListAdapter(this@ResultFragment)
    }

    private var ad:InterstitialAd? = null
    private val isAdEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adRequest = AdRequest.Builder().build()
        val adUnitId = this.getInterstitialAdId(index = 1)
        InterstitialAd.load(requireContext(), adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                ad = p0
            }
            override fun onAdFailedToLoad(p0: LoadAdError) {
                if (BuildConfig.DEBUG) {
                    Exception(p0.message).printStackTrace()
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).supportActionBar?.hide()

        binding = FragmentResultBinding.bind(view)

        if (savedInstanceState == null)
        {
            val title = arguments?.getString(ARG_TITLE)
            binding.toolBar.title = title

            val notificationId = arguments?.getInt(ServiceNotification.ARG_ID)
            notificationId?.let { id ->
                ServiceNotification.cancelNotifications(requireContext(), id)
            }
            roomSearchSet = arguments?.getSerializableCompat(ARG_SEARCH_SET, RoomSearchSet::class.java)
            roomSearchSet?.let {
                resultVM.getDealList(it.toSearchSet())
            }
        }

        binding.tradeListRV.apply {
            adapter = dealsAdapter.apply {
                showSkeleton()
            }
            setOnScrollChangeListener(object : View.OnScrollChangeListener {
                override fun onScrollChange(
                    v: View?,
                    scrollX: Int,
                    scrollY: Int,
                    oldScrollX: Int,
                    oldScrollY: Int
                ) {
                    if (oldScrollY > scrollY) {
                        binding.btnAddToTracking.setVisible(true)
                    }
                    else binding.btnAddToTracking.setVisible(false)
                }
            })
        }

        resultVM.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultViewModel.State.Initial -> {
                    binding.noResult.noResultLayout.setVisible(false)
                    binding.includedProgress.setVisible(true)
                    binding.btnAddToTracking.setVisible(false)
                }

                is ResultViewModel.State.DataReceived -> {
                    state.deals.let { list ->
                        binding.noResult.noResultLayout.visibility = View.GONE
                        binding.includedProgress.visibility = View.GONE
                        when {
                            list.size > 0 && list[0].error!!.isEmpty() -> {
                                binding.resultTV.text = list.size.toString()
                                dealsAdapter.submitList(list as List<IDeal>?)
                                return@let
                            }
                            list.size == 1 && list[0].error!!.isNotEmpty() -> {
                                binding.resultTV.text = 0.toString()
                                binding.noResult.recommendationsTV.text = requireContext().getString(R.string.text_data_not_avalible)
                                binding.noResult.noResultLayout.visibility = View.VISIBLE
                            }
                            else -> {
                                binding.resultTV.text = list.size.toString()
                                binding.noResult.noResultLayout.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                is ResultViewModel.State.DataError -> {
                    state.throwable.let {
                        binding.includedProgress.visibility = View.GONE
                        when (it) {
                            is IndexOutOfBoundsException -> {
                                with(binding) {
                                    resultTV.text = "0"
                                    noResult.noResultLayout.visibility = View.VISIBLE
                                }
                            }
                            else -> {
                                if (this.isNetworkAvailable()) {
                                    Snackbar.make(binding.tradeListRV, it.message.toString(), Snackbar.LENGTH_LONG).show()
                                } else {
                                    with(binding) {
                                        resultTV.text = 0.toString()
                                        noResult.recommendationsTV.text = requireContext().getString(R.string.text_data_not_avalible)
                                        noResult.noResultLayout.visibility = View.VISIBLE
                                        showSnackBar(getString(R.string.text_inet_not_connection))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.noResult.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddToTracking.setOnClickListener {
            val name = roomSearchSet?.generateQueryName()
            name?.let { n ->
                SaveSearchDialog.getInstance(n, listener = this).show(requireActivity().supportFragmentManager, SaveSearchDialog.TAG)
            }
        }

    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            showAdOnBackPressed(isAdEnabled)
        }

        binding.toolBar.setNavigationOnClickListener {
            showAdOnBackPressed(isAdEnabled)
        }

    }

    override fun onDestroyView() {

        (requireActivity() as MainActivity).supportActionBar?.show()
        super.onDestroyView()
    }

    override fun onRecyclerViewItemClick(deal: Deal) {
        val bundle = Bundle()
        bundle.putParcelable(DealFragment.ARG_DEAL, deal)
        requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.nav_deal, bundle)
    }

    override fun saveSearchDialogOnPositiveClick(searchName: String) {

        roomSearchSet?.apply {
            queryName = searchName
            isTracked = true
            target = Target.Tracking
            filingPeriod = 1
            tradePeriod = 3
            mainViewModel.saveSearchSet(this).observe(viewLifecycleOwner) { id ->
                if (id > 0) {
                    showSnackBar(getString(R.string.text_search_param_is_saved))
                }
                else showSnackBar("Saving error...")
            }
        }
    }

    private fun showAdOnBackPressed(flag: Boolean) {
        if (flag) {
            ad?.let {
                it.show(requireActivity())
                it.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        findNavController().popBackStack()
                    }
                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        if (BuildConfig.DEBUG) {
                            Exception(p0.message).printStackTrace()
                        }
                        findNavController().popBackStack()
                    }
                }
            } ?: run { findNavController().popBackStack() }
        }
        else findNavController().popBackStack()
    }


}
