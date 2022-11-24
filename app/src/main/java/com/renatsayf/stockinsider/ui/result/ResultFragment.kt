package com.renatsayf.stockinsider.ui.result

import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentResultBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.DealListAdapter
import com.renatsayf.stockinsider.ui.deal.DealFragment
import com.renatsayf.stockinsider.ui.dialogs.SaveSearchDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.utils.getSerializableCompat
import com.renatsayf.stockinsider.utils.showSnackBar
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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentResultBinding.bind(view)

        if (savedInstanceState == null)
        {
            val title = arguments?.getString(ARG_TITLE)
            (requireActivity() as MainActivity).supportActionBar?.title = title

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
        }

        resultVM.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultViewModel.State.Initial -> {
                    binding.noResult.noResultLayout.visibility = View.GONE
                    binding.includedProgress.visibility = View.VISIBLE
                }

                is ResultViewModel.State.DataReceived -> {
                    state.deals.let { list ->
                        binding.noResult.noResultLayout.visibility = View.GONE
                        binding.includedProgress.visibility = View.GONE
                        when {
                            list.size > 0 && list[0].error!!.isEmpty() -> {
                                binding.btnAddToTracking.visibility = View.VISIBLE
                                binding.resultTV.text = list.size.toString()
                                binding.tradeListRV.apply {
                                    dealsAdapter.addItems(list)
                                    adapter = dealsAdapter
                                }
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
                                if ((activity as MainActivity).isNetworkConnectivity()) {
                                    Snackbar.make(binding.tradeListRV, it.message.toString(), Snackbar.LENGTH_LONG).show()
                                } else {
                                    with(binding) {
                                        resultTV.text = 0.toString()
                                        noResult.recommendationsTV.text = requireContext().getString(R.string.text_data_not_avalible)
                                        noResult.noResultLayout.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }



        resultVM.addAlarmVisibility.observe(viewLifecycleOwner) {
            it?.let { binding.alertLayout.addAlarmImgView.visibility = it }
        }

        resultVM.alarmOnVisibility.observe(viewLifecycleOwner) {
            it?.let {
                binding.alertLayout.alarmOnImgView.visibility = it
            }
        }

        val title = arguments?.getString(ARG_TITLE) ?: ""
        resultVM.setToolBarTitle(title)

        binding.noResult.backButton.setOnClickListener {
            findNavController().popBackStack()
        }



        StockInsiderService.serviceEvent.observe(viewLifecycleOwner) { event ->
            if (!event.hasBeenHandled) {
                when (event.getContent()) {
                    StockInsiderService.STOP_KEY -> {
                        context?.getString(R.string.text_search_is_disabled)?.let { msg ->
                            resultVM.setAddAlarmVisibility(View.VISIBLE)
                            resultVM.setAlarmOnVisibility(View.GONE)
                            Snackbar.make(binding.tradeListRV, msg, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
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
            findNavController().popBackStack()
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        binding.includedProgress.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onRecyclerViewItemClick(deal: Deal) {
        val bundle = Bundle()
        bundle.putParcelable(DealFragment.ARG_DEAL, deal)
        requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.nav_deal, bundle)
    }

    override fun saveSearchDialogOnPositiveClick(searchName: String) {

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {

            roomSearchSet?.apply {
                queryName = searchName
                isTracked = true
                target = Target.Tracking
                filingPeriod = 1
                tradePeriod = 3
            }
            roomSearchSet?.let {
                mainViewModel.saveSearchSet(it).observe(viewLifecycleOwner) {
                    if (it > 0) {
                        showSnackBar(getString(R.string.text_search_param_is_saved))
                    }
                    else showSnackBar("Saving error...")
                }
            }

        }
    }


}
