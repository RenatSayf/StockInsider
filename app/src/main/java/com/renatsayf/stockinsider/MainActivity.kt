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
import com.renatsayf.stockinsider.databinding.ActivityMainBinding
import com.renatsayf.stockinsider.firebase.FireBaseViewModel
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.ui.adapters.ExpandableMenuAdapter
import com.renatsayf.stockinsider.ui.donate.DonateDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.ui.strategy.AppDialog
import com.renatsayf.stockinsider.ui.tracking.list.TrackingListViewModel
import com.renatsayf.stockinsider.utils.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        val APP_SETTINGS = "${this::class.java.`package`}.app_settings"
        val KEY_NO_SHOW_AGAIN = this::class.java.simpleName.plus("_key_no_show_again")
        val KEY_IS_AGREE = this::class.java.simpleName.plus("_key_is_agree")

        var interstitialAd: InterstitialAd? = null
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appDialogObserver : AppDialog.EventObserver
    lateinit var drawerLayout : DrawerLayout

    private val mainVM: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val firebaseVM: FireBaseViewModel by lazy {
        ViewModelProvider(this)[FireBaseViewModel::class.java]
    }

    private val trackedVM: TrackingListViewModel by lazy {
        ViewModelProvider(this)[TrackingListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        navController = findNavController(R.id.nav_host_fragment)
        binding.navView.apply {
            setupWithNavController(navController)
            setNavigationItemSelectedListener {
                finish()
                true
            }
        }

        firebaseVM

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        val adUnitId = this.getInterstitialAdId(index = 0)
        InterstitialAd.load(this@MainActivity, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(p0: InterstitialAd) {
                interstitialAd = p0
            }
            override fun onAdFailedToLoad(p0: LoadAdError) {
                if (BuildConfig.DEBUG) {
                    Exception(p0.message).printStackTrace()
                }
            }
        })

        binding.appBarMain.contentMain.included.loadProgressBar.setVisible(false)

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
                            trackedVM.trackedCount().observe(this@MainActivity) { count ->
                                count?.let {
                                    if (it > 0) {
                                        //startBackgroundWork()
                                        startOneTimeBackgroundWork()
                                    }
                                    interstitialAd?.let { ad ->
                                        ad.show(this@MainActivity)
                                        ad.fullScreenContentCallback =
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
                            val adId = this@MainActivity.getInterstitialAdId(index = 2)
                            InterstitialAd.load(this@MainActivity, adId, adRequest, object : InterstitialAdLoadCallback() {
                                override fun onAdLoaded(ad: InterstitialAd) {
                                    ad.show(this@MainActivity)
                                }
                                override fun onAdFailedToLoad(p0: LoadAdError) {
                                    if (BuildConfig.DEBUG) {
                                        Exception(p0.message).printStackTrace()
                                    }
                                }
                            })
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

    override fun onDestroy() {

        val nextFillingTime = AppCalendar.getNextFillingTimeByDefaultTimeZone()
        val formattedString = nextFillingTime.timeToFormattedString()
        val one = Scheduler(this).scheduleOne(nextFillingTime, 0, Scheduler.SET_NAME)
        one

        super.onDestroy()
    }

}
