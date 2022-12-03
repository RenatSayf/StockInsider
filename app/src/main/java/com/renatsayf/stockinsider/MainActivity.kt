@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.ExpandableListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.navigation.NavigationView
import com.renatsayf.stockinsider.databinding.ActivityMainBinding
import com.renatsayf.stockinsider.ui.adapters.ExpandableMenuAdapter
import com.renatsayf.stockinsider.ui.donate.DonateDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.ui.strategy.AppDialog
import com.renatsayf.stockinsider.ui.tracking.list.TrackingListViewModel
import com.renatsayf.stockinsider.utils.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity()
{
    companion object
    {
        val APP_SETTINGS = "${this::class.java.`package`}.app_settings"
        val KEY_NO_SHOW_AGAIN = this::class.java.simpleName.plus("_key_no_show_again")
        val KEY_IS_AGREE = this::class.java.simpleName.plus("_key_is_agree")

        var ad: InterstitialAd? = null
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration : AppBarConfiguration
    private lateinit var appDialogObserver : AppDialog.EventObserver
    lateinit var drawerLayout : DrawerLayout

    private val mainVM: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setTheme(R.style.AppTheme_NoActionBar)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView : NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.nav_exit) {
                finish()
            }
            true
        }

        //region TODO перед релизом удалить или закомментировать
//        appPref.edit {
//            putBoolean(KEY_NO_SHOW_AGAIN, false)
//            putBoolean(KEY_IS_AGREE, false)
//            apply()
//        }
        //endregion

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        val adUnitId = this.getInterstitialAdId(index = 0)
        InterstitialAd.load(this@MainActivity, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                ad = p0
            }
            override fun onAdFailedToLoad(p0: LoadAdError) {
                if (BuildConfig.DEBUG) {
                    Exception(p0.message).printStackTrace()
                }
            }
        })

        binding.appBarMain.contentMain.included.loadProgressBar.visibility = View.GONE

        appDialogObserver = ViewModelProvider(this)[AppDialog.EventObserver::class.java]

        val expandableMenuAdapter = ExpandableMenuAdapter(this)
        binding.expandMenu.apply {
            setAdapter(expandableMenuAdapter)
            setOnGroupClickListener(object : ExpandableListView.OnGroupClickListener {
                override fun onGroupClick(
                    p0: ExpandableListView?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ): Boolean {
                    when (p2) {
                        0 -> {
                            navController.navigate(R.id.nav_home)
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        4 -> {
                            navController.navigate(R.id.trackingListFragment)
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        5 -> {
                            when (appPref.getBoolean(KEY_NO_SHOW_AGAIN, false)) {
                                true -> {
                                    navController.navigate(R.id.nav_strategy)
                                }
                                else -> {
                                    val spannableMessage = createSpannableMessage()
                                    AppDialog.getInstance(
                                        "show_strategy",
                                        spannableMessage,
                                        context.getString(R.string.text_read),
                                        context.getString(R.string.text_close),
                                        context.getString(R.string.text_not_show_again)
                                    ).show(
                                        supportFragmentManager.beginTransaction(),
                                        AppDialog.TAG
                                    )
                                }
                            }
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        7 -> {
                            navController.navigate(R.id.nav_about_app)
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        8 -> {
                            this@MainActivity.doShare()
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        9 -> {
                            drawerLayout.closeDrawer(GravityCompat.START)
                            ad?.let {
                                it.show(this@MainActivity)
                                it.fullScreenContentCallback =
                                    object : FullScreenContentCallback() {
                                        override fun onAdDismissedFullScreenContent() {
                                            finish()
                                        }

                                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                            if (BuildConfig.DEBUG) {
                                                Exception(p0.message).printStackTrace()
                                            }
                                            finish()
                                        }
                                    }
                            } ?: run { finish() }
                        }
                    }
                    return false
                }
            })
            setOnChildClickListener(object : ExpandableListView.OnChildClickListener {
                override fun onChildClick(
                    p0: ExpandableListView?,
                    p1: View?,
                    p2: Int,
                    p3: Int,
                    p4: Long
                ): Boolean {
                    when {
                        p2 == 1 && p3 == 0 -> {
                            mainVM.getSearchSetByName("pur_more1_for_3")
                                .observe(this@MainActivity) {
                                    Bundle().apply {
                                        putString(
                                            ResultFragment.ARG_TITLE,
                                            context.getString(R.string.text_pur_more1_for_3)
                                        )
                                        putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                    }.run { navController.navigate(R.id.nav_result, this) }
                                }
                        }
                        p2 == 1 && p3 == 1 -> {
                            mainVM.getSearchSetByName("pur_more5_for_3")
                                .observe(this@MainActivity) {
                                    Bundle().apply {
                                        putString(
                                            ResultFragment.ARG_TITLE,
                                            context.getString(R.string.text_pur_more5_for_3)
                                        )
                                        putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                    }.run { navController.navigate(R.id.nav_result, this) }
                                }
                        }
                        p2 == 1 && p3 == 2 -> {
                            mainVM.getSearchSetByName("sale_more1_for_3")
                                .observe(this@MainActivity) {
                                    Bundle().apply {
                                        putString(
                                            ResultFragment.ARG_TITLE,
                                            context.getString(R.string.text_sale_more1_for_3)
                                        )
                                        putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                    }.run { navController.navigate(R.id.nav_result, this) }
                                }

                        }
                        p2 == 1 && p3 == 3 -> {
                            mainVM.getSearchSetByName("sale_more5_for_3")
                                .observe(this@MainActivity) {
                                    Bundle().apply {
                                        putString(
                                            ResultFragment.ARG_TITLE,
                                            context.getString(R.string.text_sale_more5_for_3)
                                        )
                                        putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                    }.run { navController.navigate(R.id.nav_result, this) }
                                }
                        }
                        p2 == 2 && p3 == 0 -> {
                            mainVM.getSearchSetByName("purchases_more_1")
                                .observe(this@MainActivity) {
                                    val bundle = Bundle().apply {
                                        putString(
                                            ResultFragment.ARG_TITLE,
                                            context.getString(R.string.text_purchases_more_1)
                                        )
                                        putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                    }
                                    navController.navigate(R.id.nav_result, bundle)
                                }
                        }
                        p2 == 2 && p3 == 1 -> {
                            mainVM.getSearchSetByName("purchases_more_5")
                                .observe(this@MainActivity) {
                                    val bundle = Bundle().apply {
                                        putString(
                                            ResultFragment.ARG_TITLE,
                                            context.getString(R.string.text_purchases_more_5)
                                        )
                                        putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                    }
                                    navController.navigate(R.id.nav_result, bundle)
                                }
                        }
                        p2 == 2 && p3 == 2 -> {
                            mainVM.getSearchSetByName("sales_more_1").observe(this@MainActivity) {
                                Bundle().apply {
                                    putString(
                                        ResultFragment.ARG_TITLE,
                                        context.getString(R.string.text_sales_more_1)
                                    )
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                }.run { navController.navigate(R.id.nav_result, this) }
                            }
                        }
                        p2 == 2 && p3 == 3 -> {
                            mainVM.getSearchSetByName("sales_more_5").observe(this@MainActivity) {
                                Bundle().apply {
                                    putString(
                                        ResultFragment.ARG_TITLE,
                                        context.getString(R.string.text_sales_more_5)
                                    )
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                }.run { navController.navigate(R.id.nav_result, this) }
                            }
                        }
                        p2 == 3 && p3 == 0 -> {
                            mainVM.getSearchSetByName("pur_more1_for_14")
                                .observe(this@MainActivity) {
                                    Bundle().apply {
                                        putString(
                                            ResultFragment.ARG_TITLE,
                                            context.getString(R.string.text_pur_more1_for_14)
                                        )
                                        putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                    }.run { navController.navigate(R.id.nav_result, this) }
                                }
                        }
                        p2 == 3 && p3 == 1 -> {
                            mainVM.getSearchSetByName("pur_more5_for_14")
                                .observe(this@MainActivity) {
                                    Bundle().apply {
                                        putString(
                                            ResultFragment.ARG_TITLE,
                                            context.getString(R.string.text_pur_more5_for_14)
                                        )
                                        putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                    }.run { navController.navigate(R.id.nav_result, this) }
                                }
                        }
                        p2 == 3 && p3 == 2 -> {
                            mainVM.getSearchSetByName("sale_more1_for_14")
                                .observe(this@MainActivity) {
                                    Bundle().apply {
                                        putString(
                                            ResultFragment.ARG_TITLE,
                                            context.getString(R.string.text_sale_more1_for_14)
                                        )
                                        putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                    }.run { navController.navigate(R.id.nav_result, this) }
                                }
                        }
                        p2 == 3 && p3 == 3 -> {
                            mainVM.getSearchSetByName("sale_more5_for_14")
                                .observe(this@MainActivity) {
                                    Bundle().apply {
                                        putString(
                                            ResultFragment.ARG_TITLE,
                                            context.getString(R.string.text_sale_more5_for_14)
                                        )
                                        putSerializable(ResultFragment.ARG_SEARCH_SET, it)
                                    }.run { navController.navigate(R.id.nav_result, this) }
                                }
                        }
                        p2 == 6 && p3 == 0 -> {
                            if (this@MainActivity.isNetworkAvailable()) {
                                DonateDialog.getInstance()
                                    .show(supportFragmentManager, DonateDialog.TAG)
                            } else binding.expandMenu.showSnackBar(getString(R.string.text_inet_not_connection))
                        }
                        p2 == 6 && p3 == 1 -> {
                            ad?.let {
                                it.show(this@MainActivity)
                                it.fullScreenContentCallback =
                                    object : FullScreenContentCallback() {
                                        override fun onAdDismissedFullScreenContent() {}
                                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                            if (BuildConfig.DEBUG) {
                                                Exception(p0.message).printStackTrace()
                                            }
                                        }
                                    }
                            }
                        }
                    }
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return false
                }
            })
        }

        appDialogObserver.data.observe(this) { event ->
            event.getContent()?.let {
                if (it.first == "show_strategy") {
                    when (it.second) {
                        -1 -> {
                            navController.navigate(R.id.nav_strategy)
                        }
                        -3 -> {
                            appPref.edit {
                                putBoolean(KEY_NO_SHOW_AGAIN, true)
                                apply()
                            }
                            navController.navigate(R.id.nav_strategy)
                        }
                    }
                }
            }
        }

    }

    private fun createSpannableMessage() : SpannableStringBuilder
    {
        val clickable = object : ClickableSpan()
        {
            override fun onClick(p0: View)
            {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.fxmag.ru/"))
                startActivity(intent)
            }
        }
        val string1 = getString(R.string.text_hi)+"\n"
        val spannable1 = SpannableString(string1)
        spannable1.apply {
            setSpan(ForegroundColorSpan(Color.GREEN),
                0,
                string1.length - 1,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            setSpan(RelativeSizeSpan(2f), 0, string1.length - 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        }
        val spannableStringBuilder = SpannableStringBuilder(spannable1)

        val string2 = getString(R.string.text_strategy_dialog_1)+"\n"

        spannableStringBuilder.append(SpannableString(string2).apply {
            setSpan(UnderlineSpan(), 71, 85, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(getColor(R.color.colorSectionDivider)),
                71,
                85,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            setSpan(clickable, 71, 85, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        })

        val string3 = "\n"+getString(R.string.text_strategy_dialog_2)
        spannableStringBuilder.append(SpannableString(string3))
        return spannableStringBuilder
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    private val trackedVM: TrackingListViewModel by lazy {
        ViewModelProvider(this)[TrackingListViewModel::class.java]
    }

    override fun onDestroy() {

        val count = trackedVM.trackedCount.value
        if (count != null && count > 0) {
            startBackgroundWork()
        }
        super.onDestroy()
    }

}
