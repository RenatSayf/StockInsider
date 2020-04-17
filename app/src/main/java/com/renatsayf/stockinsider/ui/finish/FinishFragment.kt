package com.renatsayf.stockinsider.ui.finish

import android.os.Bundle
import androidx.fragment.app.Fragment

class FinishFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        activity?.finish()
    }
}