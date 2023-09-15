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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.gms.ads.*
import com.renatsayf.stockinsider.databinding.ActivityMainBinding
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.receivers.AlarmReceiver
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.service.notifications.ServiceNotification
import com.renatsayf.stockinsider.ui.ad.AdViewModel
import com.renatsayf.stockinsider.ui.ad.AdsId
import com.renatsayf.stockinsider.ui.adapters.ExpandableMenuAdapter
import com.renatsayf.stockinsider.ui.donate.DonateDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.ui.strategy.AppDialog
import com.renatsayf.stockinsider.ui.tracking.list.TrackingListViewModel
import com.renatsayf.stockinsider.utils.*
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.impl.kv
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.rewarded.RewardedAd
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        val APP_SETTINGS = "${this::class.java.`package`}.app_settings"
        val KEY_NO_SHOW_AGAIN = this::class.java.simpleName.plus("_key_no_show_again")
        val KEY_IS_AGREE = this::class.java.simpleName.plus("_key_is_agree")
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appDialogObserver : AppDialog.EventObserver
    lateinit var drawerLayout : DrawerLayout

    private val mainVM: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val trackedVM: TrackingListViewModel by lazy {
        ViewModelProvider(this)[TrackingListViewModel::class.java]
    }

    private val adVM: AdViewModel by viewModels()
    private var yandexAd0: RewardedAd? = null
    var yandexAd1: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        navController = findNavController(R.id.nav_host_fragment)
        binding.navView.apply {
            setupWithNavController(navController)
        }

        if (savedInstanceState == null) {
            FireBaseConfig
            adVM.loadRewardedAd(adId = AdsId.REWARDED_1, false, object : AdViewModel.YandexAdListener {
                override fun onYandexAdLoaded(
                    ad: kv,
                    isOnExit: Boolean
                ) {
                    yandexAd0 = ad as RewardedAd
                }
                override fun onYandexAdFailed(error: AdRequestError) {
                    yandexAd0 = null
                    if (BuildConfig.DEBUG) println("************* ${error.description} ****************")
                }
            })

            adVM.loadInterstitialAd(adId = AdsId.INTERSTITIAL_1, false, object : AdViewModel.YandexAdListener {
                override fun onYandexAdLoaded(
                    ad: kv,
                    isOnExit: Boolean
                ) {
                    yandexAd1 = ad as InterstitialAd
                }
                override fun onYandexAdFailed(error: AdRequestError) {
                    yandexAd1 = null
                    if (BuildConfig.DEBUG) println("************* ${error.description} ****************")
                }
            })
        }

        binding.appBarMain.contentMain.included.loadProgressBar.setVisible(false)

        appDialogObserver = ViewModelProvider(this)[AppDialog.EventObserver::class.java]

        val expandableMenuAdapter = ExpandableMenuAdapter(this)
        binding.expandMenu.apply {
            setAdapter(expandableMenuAdapter)
            setOnGroupClickListener(object : ExpandableListView.OnGroupClickListener {
                override fun onGroupClick(
                    p0: ExpandableListView?,
                    p1: View?,
                    item: Int,
                    p3: Long
                ): Boolean {
                    when (item) {
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
                                        val nextTime = setAlarm(
                                            scheduler = Scheduler(this@MainActivity, AlarmReceiver::class.java),
                                            periodInMinute = FireBaseConfig.trackingPeriod
                                        )
                                        if (nextTime != null) {
                                            val message = "${getString(R.string.text_next_check_will_be_at)} ${nextTime.timeToFormattedStringWithoutSeconds()}"
                                            ServiceNotification.notify(this@MainActivity, message, null)
                                        }
                                    }
                                }
                                showInterstitialAd(yandexAd1) {
                                    finish()
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
                    item: Int,
                    subItem: Int,
                    p4: Long
                ): Boolean {
                    when {
                        item == 1 && subItem == 0 -> {
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
                        item == 1 && subItem == 1 -> {
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
                        item == 1 && subItem == 2 -> {
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
                        item == 1 && subItem == 3 -> {
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
                        item == 2 && subItem == 0 -> {
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
                        item == 2 && subItem == 1 -> {
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
                        item == 2 && subItem == 2 -> {
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
                        item == 2 && subItem == 3 -> {
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
                        item == 3 && subItem == 0 -> {
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
                        item == 3 && subItem == 1 -> {
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
                        item == 3 && subItem == 2 -> {
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
                        item == 3 && subItem == 3 -> {
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
                        item == 6 && subItem == 0 -> {
                            if (this@MainActivity.isNetworkAvailable()) {
                                DonateDialog.getInstance()
                                    .show(supportFragmentManager, DonateDialog.TAG)
                            } else binding.expandMenu.showSnackBar(getString(R.string.text_inet_not_connection))
                        }
                        item == 6 && subItem == 1 -> {
                            showRewardedAd(yandexAd0)
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

}


