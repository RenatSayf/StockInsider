package com.renatsayf.stockinsider.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.renatsayf.stockinsider.R
import kotlinx.android.synthetic.main.fragment_result.*

class ResultFragment : Fragment()
{

    companion object
    {
        val TAG: String? = "result_fragment"
    }

    private lateinit var viewModel : ResultViewModel

    override fun onCreateView(
            inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
                             ) : View?
    {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ResultViewModel::class.java)

        val array = arguments?.getStringArrayList(TAG)
        val s = array?.get(0)
        resTextView.text = s

    }

}
