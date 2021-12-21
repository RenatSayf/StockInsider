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

class TrackingFragment : Fragment(R.layout.tracking_fragment) {

    private lateinit var binding: TrackingFragmentBinding

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

        binding.includedToolBar.toolBarBtnBack.setOnClickListener {
            requireActivity().findNavController(R.id.nav_host_fragment).popBackStack()
        }
    }

    override fun onDestroy() {
        (activity as MainActivity).supportActionBar?.show()

        super.onDestroy()
    }

}