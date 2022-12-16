@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.result

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentResultBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.firebase.FireBaseViewModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.ResultData
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.deal.DealFragment
import com.renatsayf.stockinsider.ui.dialogs.InfoDialog
import com.renatsayf.stockinsider.ui.dialogs.SaveSearchDialog
import com.renatsayf.stockinsider.ui.dialogs.SortingDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.sorting.SortingViewModel
import com.renatsayf.stockinsider.ui.tracking.list.TrackingListViewModel
import com.renatsayf.stockinsider.utils.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ResultFragment : Fragment(R.layout.fragment_result), DealListAdapter.Listener, SaveSearchDialog.Listener,
    SortingDialog.Listener {

    companion object
    {
        val TAG = "${this::class.java.simpleName}.TAG"
        val ARG_SEARCH_SET = "${this::class.java.simpleName}.ARG_SEARCH_SET"
        val ARG_TITLE = "${this::class.java.simpleName}.ARG_TITLE"
    }

    private lateinit var binding: FragmentResultBinding
    private val resultVM : ResultViewModel by viewModels()
    private val mainViewModel : MainViewModel by viewModels()
    private val sortingVM: SortingViewModel by viewModels()
    private val trackingVM: TrackingListViewModel by viewModels()
    private var roomSearchSet: RoomSearchSet? = null

    private val dealsAdapter: DealListAdapter by lazy {
        DealListAdapter(this)
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
                        val layoutParams = binding.btnAddToTracking.layoutParams as ConstraintLayout.LayoutParams
                        val bottomMargin = layoutParams.bottomMargin
                        binding.btnAddToTracking.setVisible(true)
                        ValueAnimator.ofInt(bottomMargin, 64.dp).apply {
                            addUpdateListener {
                                layoutParams.bottomMargin = it.animatedValue as Int
                                binding.btnAddToTracking.layoutParams = layoutParams
                            }
                        }.setDuration(300).start()
                    }
                    else {
                        val layoutParams = binding.btnAddToTracking.layoutParams as ConstraintLayout.LayoutParams
                        val bottomMargin = layoutParams.bottomMargin
                        ValueAnimator.ofInt(bottomMargin, -138).apply {
                            addUpdateListener {
                                layoutParams.bottomMargin = it.animatedValue as Int
                                binding.btnAddToTracking.layoutParams = layoutParams
                            }
                        }.setDuration(300).start()
                    }
                }
            })
        }

        resultVM.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultViewModel.State.Initial -> {
                    binding.noResult.noResultLayout.setVisible(false)
                    binding.includedProgress.setVisible(true)
                    binding.btnAddToTracking.setVisible(false)
                    roomSearchSet?.let {
                        resultVM.getDealList(it.toSearchSet())
                    }
                }
                is ResultViewModel.State.DataReceived -> {
                    state.deals.let { list ->
                        binding.noResult.noResultLayout.setVisible(false)
                        binding.includedProgress.setVisible(false)
                        when {
                            list.size > 0 && list[0].error!!.isEmpty() -> {
                                binding.resultTV.text = list.size.toString()
                                binding.tradeListRV.apply {
                                    val sorting = sortingVM.sorting
                                    val map = sortingVM.doSort(list, sorting)
                                    dealsAdapter.replaceItems(map, sorting)
                                }
                                return@let
                            }
                            list.size == 1 && list[0].error!!.isNotEmpty() -> {
                                binding.resultTV.text = 0.toString()
                                binding.noResult.recommendationsTV.text = requireContext().getString(R.string.text_data_not_avalible)
                                binding.noResult.noResultLayout.setVisible(true)
                            }
                            else -> {
                                binding.resultTV.text = list.size.toString()
                                binding.noResult.noResultLayout.setVisible(true)
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
                                    showSnackBar(it.message.toString())
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
                is ResultViewModel.State.DataSorted -> {
                    val dealsMap = state.dealsMap
                    if (dealsMap.isNotEmpty()) {
                        binding.resultTV.text = dealsMap.size.toString()
                        binding.noResult.noResultLayout.setVisible(false)
                        binding.includedProgress.setVisible(false)
                        dealsAdapter.replaceItems(dealsMap, sortingVM.sorting)
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
        binding.btnSorting.apply {
            setOnClickListener {
                SortingDialog.instance(sortingVM.sorting, this@ResultFragment).show(childFragmentManager, SortingDialog.TAG)
            }
        }

    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as MainActivity).supportActionBar?.hide()

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
        findNavController().navigate(R.id.nav_deal, bundle)
    }

    override fun onSaveSearchDialogPositiveClick(searchName: String) {

        val set = roomSearchSet?.apply {
            queryName = searchName
            target = Target.Tracking
            filingPeriod = 1
            tradePeriod = 3
            isDefault = false
        }
        set?.let { s ->
            trackingVM.targetCount().observe(viewLifecycleOwner) { count ->
                count?.let { c ->
                    if (c < FireBaseViewModel.requestsCount) {
                        s.isTracked = true
                        mainViewModel.addNewSearchSet(s).observe(viewLifecycleOwner) { res ->
                            when(res) {
                                is ResultData.Error -> {
                                    showInfoDialog(
                                        title = "Saving error...", message = res.message, status = InfoDialog.DialogStatus.ERROR
                                    )
                                }
                                is ResultData.Success -> {
                                    if (res.data > 0) {
                                        showInfoDialog(
                                            title = getString(R.string.text_success),
                                            message = getString(R.string.text_search_param_is_saved),
                                            status = InfoDialog.DialogStatus.SUCCESS
                                        )
                                        findNavController().navigate(R.id.trackingListFragment)
                                    }
                                    else {
                                        showInfoDialog(
                                            title = getString(R.string.text_warning),
                                            message = getString(R.string.text_search_already_exists),
                                            status = InfoDialog.DialogStatus.WARNING
                                        )
                                    }
                                }
                                ResultData.Init -> {}
                            }
                        }
                    }
                    else {
                        s.let {
                            it.target = null
                            it.isTracked = false
                            mainViewModel.addNewSearchSet(it).observe(viewLifecycleOwner) { res ->
                                when(res) {
                                    ResultData.Init -> {}
                                    is ResultData.Error -> {
                                        showInfoDialog(
                                            title = "Saving error...", message = res.message, status = InfoDialog.DialogStatus.ERROR
                                        )
                                    }
                                    is ResultData.Success -> {
                                        if (res.data > 0) {
                                            showInfoDialog(
                                                title = getString(R.string.text_warning),
                                                message = getString(R.string.text_search_params_is_saved_but_not_tracked),
                                                status = InfoDialog.DialogStatus.WARNING
                                            )
                                        }
                                        else {
                                            showInfoDialog(
                                                title = getString(R.string.text_warning),
                                                message = getString(R.string.text_search_already_exists),
                                                status = InfoDialog.DialogStatus.WARNING
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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

    override fun onSortingDialogButtonClick(sorting: SortingViewModel.Sorting) {
        val currentList = dealsAdapter.getDeals()
        if (currentList.isNotEmpty()) {
            val sortedMap = sortingVM.doSort(currentList.toList(), sorting)
            resultVM.setState(ResultViewModel.State.DataSorted(sortedMap))
        }
    }


}
