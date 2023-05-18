package com.renatsayf.stockinsider.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        lifecycleScope.launch {
            delay(2000)
            val intent = Intent(this@LaunchActivity, MainActivity::class.java)
            startActivity(intent)
            this@LaunchActivity.finish()
        }
    }
}