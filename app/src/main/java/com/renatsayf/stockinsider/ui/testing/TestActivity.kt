package com.renatsayf.stockinsider.ui.testing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.renatsayf.stockinsider.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }
}