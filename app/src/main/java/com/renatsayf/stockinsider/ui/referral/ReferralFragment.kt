@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.renatsayf.stockinsider.ui.referral

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentReferralBinding
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.ui.referral.models.Brokers
import com.renatsayf.stockinsider.utils.startBrowserSearch
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReferralFragment : Fragment() {

    private lateinit var binding: FragmentReferralBinding

    private val referralVM by viewModels<ReferralViewModel>()
    private val brokerListAdapter: BrokerListAdapter by lazy {
        BrokerListAdapter.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReferralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            if (savedInstanceState == null) {
                referralVM.fetchPartners(FireBaseConfig)
            }

            rvPartners.adapter = brokerListAdapter

            referralVM.brokers.observe(viewLifecycleOwner) { res ->
                res.onSuccess<Brokers> { brokers ->
                    brokerListAdapter.submitList(brokers.list)
                }
                res.onError { message, i ->
                    tvPageDescription.text = getString(R.string.text_broker_list_load_error)
                }
            }
            brokerListAdapter.brokerReference.observe(viewLifecycleOwner) { res ->
                res.onSuccess<String> { ref ->
                    startBrowserSearch(ref)
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

}