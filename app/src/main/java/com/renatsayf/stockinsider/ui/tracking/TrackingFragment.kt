package com.renatsayf.stockinsider.ui.tracking

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TrackingFragmentBinding
import com.renatsayf.stockinsider.ui.adapters.TickersListAdapter
import com.renatsayf.stockinsider.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.tracking_fragment) {

    companion object {
        val ARG_SET = "${this::class.java.simpleName}.set"
        val ARG_TITLE = "${this::class.java.simpleName}.title"
    }

    private lateinit var binding: TrackingFragmentBinding
    private val mainVM: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tracking_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = TrackingFragmentBinding.bind(view)

        (activity as MainActivity).supportActionBar?.hide()

        val title = arguments?.getString(ARG_TITLE)

        with(binding) {

            includedToolBar.appToolbar.title = title

            includedToolBar.appToolbar.setNavigationOnClickListener {
                requireActivity().findNavController(R.id.nav_host_fragment).popBackStack()
            }

            mainVM.companies.observe(viewLifecycleOwner, { companies ->
                companies?.let {
                    val tickersListAdapter = TickersListAdapter(requireActivity(), it)
                    tickersView.setAdapter(tickersListAdapter)
                }
            })
        }


    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        (activity as MainActivity).supportActionBar?.show()
        super.onStop()
    }

    override fun onDestroy() {

        super.onDestroy()
    }

}