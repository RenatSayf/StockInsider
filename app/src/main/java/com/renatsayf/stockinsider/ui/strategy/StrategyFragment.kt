@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.strategy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentStrategyBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StrategyFragment : Fragment(R.layout.fragment_strategy)
{
    companion object
    {
        private var instance: StrategyFragment? = null
        fun getInstance(): StrategyFragment = if (instance == null) StrategyFragment() else instance as StrategyFragment
    }

    private lateinit var binding: FragmentStrategyBinding

    override fun onCreateView(inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_strategy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentStrategyBinding.bind(view)

        binding.webView.apply {
            val currentLocale = ConfigurationCompat.getLocales(resources.configuration)[0]
            val language = currentLocale?.language
            if (language == "ru") {
                loadUrl("file:///android_asset/strategy/index.html")
            }
            else {
                loadUrl("file:///android_asset/strategy/index-en.html")
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            findNavController().popBackStack()
        }
    }

}