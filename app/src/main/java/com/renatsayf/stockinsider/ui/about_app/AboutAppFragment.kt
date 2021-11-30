package com.renatsayf.stockinsider.ui.about_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.AboutAppFragmentBinding


class AboutAppFragment : Fragment(R.layout.about_app_fragment)
{
    private lateinit var binding: AboutAppFragmentBinding

    companion object
    {
        fun getInstance() = AboutAppFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.about_app_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding = AboutAppFragmentBinding.bind(view)

        val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        val versionName = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P)
        {
            "v.${packageInfo.versionName}.${packageInfo.longVersionCode}"
        }
        else
        {
            "v.${packageInfo.versionName}"
        }
        binding.versionNameView.text = versionName

        binding.btnEvaluate.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(getString(R.string.app_link).plus(packageInfo.packageName))
            startActivity(intent)
        }

        binding.privacyPolicyLinkView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_link)))
            startActivity(intent)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this){
            (activity as MainActivity).navController.navigate(R.id.nav_home)
        }
    }

}