package com.renatsayf.stockinsider.ui.about_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.AboutAppFragmentBinding
import com.renatsayf.stockinsider.utils.goToAppStore
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AboutAppFragment : Fragment() {
    private lateinit var binding: AboutAppFragmentBinding

    companion object {
        fun getInstance() = AboutAppFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AboutAppFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        val versionName = "v.${packageInfo.versionName}"

        binding.versionNameView.text = versionName

        binding.btnEvaluate.setOnClickListener {
            requireContext().goToAppStore()
        }

        binding.privacyPolicyLinkView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_link)))
            startActivity(intent)
        }

        binding.toolBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

}