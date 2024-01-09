package com.renatsayf.stockinsider.ui.referral

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.renatsayf.stockinsider.databinding.FragmentReferralBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReferralFragment : Fragment() {

    private lateinit var binding: FragmentReferralBinding

    private val referralVM by viewModels<ReferralViewModel>()

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


        }
    }

}