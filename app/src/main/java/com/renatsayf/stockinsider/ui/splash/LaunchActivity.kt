package com.renatsayf.stockinsider.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.ui.settings.KEY_APP_STORE_LINK
import com.renatsayf.stockinsider.ui.settings.KEY_URL
import com.renatsayf.stockinsider.utils.goToAppStore
import com.renatsayf.stockinsider.utils.goToUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        val extras = intent.extras
        val appStoreLink = extras?.getString(KEY_APP_STORE_LINK)
        if (!appStoreLink.isNullOrEmpty()) {
            goToAppStore()
            finish()
        }

        val url = extras?.getString(KEY_URL)
        if (!url.isNullOrEmpty()) {
            goToUrl(url)
            finish()
        }

        lifecycleScope.launch {
            delay(2000)
            val intent = Intent(this@LaunchActivity, MainActivity::class.java)
            startActivity(intent)
            this@LaunchActivity.finish()
        }
    }
}