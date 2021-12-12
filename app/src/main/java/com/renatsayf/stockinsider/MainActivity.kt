@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import android.view.inputmethod.InputMethodManager
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.databinding.ActivityMainBinding
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.ExpandableMenuAdapter
import com.renatsayf.stockinsider.ui.donate.DonateDialog
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.ui.strategy.AppDialog
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity()
{
    companion object
    {
        val APP_SETTINGS = "${this::class.java.`package`}.app_settings"
        val KEY_NO_SHOW_AGAIN = this::class.java.simpleName.plus("_key_no_show_again")
        val KEY_IS_AGREE = this::class.java.simpleName.plus("_key_is_agree")
    }

    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    private lateinit var appBarConfiguration : AppBarConfiguration
    private lateinit var appDialogObserver : AppDialog.EventObserver
    private lateinit var drawerLayout : DrawerLayout
    private val mainVM: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private lateinit var ad: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setTheme(R.style.AppTheme_NoActionBar)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)
        ad = InterstitialAd(this)

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
            if (item.itemId == R.id.nav_exit)
            {
                finish()
            }
            true
        }

        //region TODO перед релизом удалить или закомментировать
//        getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE).edit {
//            putBoolean(KEY_NO_SHOW_AGAIN, false)
//            //putBoolean(KEY_IS_AGREE, false)
//            apply()
//        }
        //endregion

        binding.appBarMain.contentMain.included.loadProgressBar.visibility = View.GONE

        appDialogObserver = ViewModelProvider(this)[AppDialog.EventObserver::class.java]

        val expandableMenuAdapter = ExpandableMenuAdapter(this)
        binding.expandMenu.apply {
            setAdapter(expandableMenuAdapter)
            setOnGroupClickListener(object : ExpandableListView.OnGroupClickListener
            {
                override fun onGroupClick(p0: ExpandableListView?,
                    p1: View?,
                    p2: Int,
                    p3: Long): Boolean
                {
                    when (p2)
                    {
                        0 ->
                        {
                            navController.navigate(R.id.nav_home)
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        4 ->
                        {
                            navController.navigate(R.id.action_nav_home_to_trackingListFragment)
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        5 ->
                        {
                            when (getSharedPreferences(APP_SETTINGS,
                                Context.MODE_PRIVATE).getBoolean(KEY_NO_SHOW_AGAIN, false))
                            {
                                true ->
                                {
                                    navController.navigate(R.id.nav_strategy)
                                }
                                else ->
                                {
                                    val spannableMessage = createSpannableMessage()
                                    AppDialog.getInstance("show_strategy",
                                        spannableMessage,
                                        context.getString(R.string.text_read),
                                        context.getString(R.string.text_close),
                                        context.getString(R.string.text_not_show_again)).show(
                                        supportFragmentManager.beginTransaction(),
                                        AppDialog.TAG)
                                }
                            }
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        7 ->
                        {
                            navController.navigate(R.id.nav_about_app)
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        8 ->
                        {
                            doShare()
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        9 ->
                        {
                            drawerLayout.closeDrawer(GravityCompat.START)
                            if (isNetworkConnectivity())
                            {
                                if (ad.isLoaded) ad.show() else finish()
                                ad.adListener = object : AdListener(){
                                    override fun onAdClosed() {
                                        finish()
                                    }
                                }
                            }
                            else finish()
                        }
                    }
                    return false
                }
            })
            setOnChildClickListener(object : ExpandableListView.OnChildClickListener
            {
                override fun onChildClick(p0: ExpandableListView?,
                    p1: View?,
                    p2: Int,
                    p3: Int,
                    p4: Long): Boolean
                {
                    when
                    {
                        p2 == 1 && p3 == 0 ->
                        {
                            mainVM.getCurrentSearchSet("pur_more1_for_3").observe(this@MainActivity, {
                                    Bundle().apply {
                                        putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_pur_more1_for_3))
                                        putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                    }.run { navController.navigate(R.id.nav_result, this) }
                                })
                        }
                        p2 == 1 && p3 == 1 ->
                        {
                            mainVM.getCurrentSearchSet("pur_more5_for_3").observe(this@MainActivity, {
                                Bundle().apply {
                                    putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_pur_more5_for_3))
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                }.run { navController.navigate(R.id.nav_result, this) }
                            })
                        }
                        p2 == 1 && p3 == 2 ->
                        {
                            mainVM.getCurrentSearchSet("sale_more1_for_3").observe(this@MainActivity, {
                                Bundle().apply {
                                    putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sale_more1_for_3))
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                }.run { navController.navigate(R.id.nav_result, this) }
                            })

                        }
                        p2 == 1 && p3 == 3 ->
                        {
                            mainVM.getCurrentSearchSet("sale_more5_for_3").observe(this@MainActivity, {
                                Bundle().apply {
                                    putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sale_more5_for_3))
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                }.run { navController.navigate(R.id.nav_result, this) }
                            })
                        }
                        p2 == 2 && p3 == 0 ->
                        {
                            mainVM.getCurrentSearchSet("purchases_more_1").observe(this@MainActivity, {
                                val bundle = Bundle().apply {
                                    putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_purchases_more_1))
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                }
                                navController.navigate(R.id.nav_result, bundle)
                            })
                        }
                        p2 == 2 && p3 == 1 ->
                        {
                            mainVM.getCurrentSearchSet("purchases_more_5").observe(this@MainActivity, {
                                val bundle = Bundle().apply {
                                    putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_purchases_more_5))
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                }
                                navController.navigate(R.id.nav_result, bundle)
                            })
                        }
                        p2 == 2 && p3 == 2 ->
                        {
                            mainVM.getCurrentSearchSet("sales_more_1").observe(this@MainActivity, {
                                Bundle().apply {
                                    putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sales_more_1))
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                }.run { navController.navigate(R.id.nav_result, this) }
                            })
                        }
                        p2 == 2 && p3 == 3 ->
                        {
                            mainVM.getCurrentSearchSet("sales_more_5").observe(this@MainActivity, {
                                Bundle().apply {
                                    putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sales_more_5))
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                }.run { navController.navigate(R.id.nav_result, this) }
                            })
                        }
                        p2 == 3 && p3 == 0 ->
                        {
                            mainVM.getCurrentSearchSet("pur_more1_for_14").observe(this@MainActivity, {
                                Bundle().apply {
                                    putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_pur_more1_for_14))
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                }.run { navController.navigate(R.id.nav_result, this) }
                            })
                        }
                        p2 == 3 && p3 == 1 ->
                        {
                            mainVM.getCurrentSearchSet("pur_more5_for_14").observe(this@MainActivity, {
                                Bundle().apply {
                                    putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_pur_more5_for_14))
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                }.run { navController.navigate(R.id.nav_result, this) }
                            })
                        }
                        p2 == 3 && p3 == 2 ->
                        {
                            mainVM.getCurrentSearchSet("sale_more1_for_14").observe(this@MainActivity, {
                                Bundle().apply {
                                    putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sale_more1_for_14))
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                }.run { navController.navigate(R.id.nav_result, this) }
                            })
                        }
                        p2 == 3 && p3 == 3 ->
                        {
                            mainVM.getCurrentSearchSet("sale_more5_for_14").observe(this@MainActivity, {
                                Bundle().apply {
                                    putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sale_more5_for_14))
                                    putSerializable(ResultFragment.ARG_SEARCH_SET, it.toSearchSet())
                                }.run { navController.navigate(R.id.nav_result, this) }
                            })
                        }
                        p2 == 5 && p3 == 0 ->
                        {
                            if (isNetworkConnectivity())
                            {
                                DonateDialog.getInstance().show(supportFragmentManager, DonateDialog.TAG)
                            }
                        }
                        p2 == 5 && p3 == 1 ->
                        {
                            if (isNetworkConnectivity())
                            {
                                if (ad.isLoaded) ad.show() else ad.loadAd(AdRequest.Builder().build())
                                ad.adListener = object : AdListener()
                                {
                                    override fun onAdClosed() {
                                        ad.loadAd(AdRequest.Builder().build())
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

        appDialogObserver.data.observe(this, { event ->
            event.getContent()?.let {
                if (it.first == "show_strategy")
                {
                    when (it.second)
                    {
                        -1 ->
                        {
                            navController.navigate(R.id.nav_strategy)
                        }
                        -3 ->
                        {
                            getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE).edit {
                                putBoolean(KEY_NO_SHOW_AGAIN, true)
                                apply()
                            }
                            navController.navigate(R.id.nav_strategy)
                        }
                    }
                }
            }
        })

        ad.apply {
            adUnitId = if (BuildConfig.DEBUG)
            {
                getString(R.string.test_interstitial_ads_id)
            }
            else
            {
                getString(R.string.interstitial_ad_1)
            }
            loadAd(AdRequest.Builder().build())
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

    override fun onSupportNavigateUp() : Boolean
    {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun setAlarmSetting(alarm: Boolean)
    {
        getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE).edit {
            putBoolean(AlarmPendingIntent.IS_ALARM_SETUP_KEY, alarm)
            apply()
        }.also {
            if (!alarm)
            {
                val serviceIntent = Intent(this, StockInsiderService::class.java)
                stopService(serviceIntent)
            }
        }
    }

    fun hideKeyBoard(view: View)
    {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun isServiceRunning() : Boolean
    {
        val activityManager = this.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        val services = activityManager.getRunningServices(Int.MAX_VALUE)
        services.forEach {
            when(it.service.packageName)
            {
                this.packageName ->
                {
                    return true
                }
            }
        }
        return false
    }

    //TODO INTERNET connection checking function
    fun isNetworkConnectivity(): Boolean
    {
        val cm: ConnectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        activeNetwork?.let { network ->
            val nc = cm.getNetworkCapabilities(network)
            nc?.let {
                return(it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || it.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI))
            }
        }
        Snackbar.make(binding.expandMenu,
            getString(R.string.text_inet_not_connection),
            Snackbar.LENGTH_LONG).show()
        return false
    }

    fun doShare()
    {
        val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody = "http://play.google.com/store/apps/details?id=" + this.packageName
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.text_share_using)))
    }

    override fun finish()
    {
        super.finish()
        val isServiceEnabled = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE).getBoolean(
            AlarmPendingIntent.IS_ALARM_SETUP_KEY,
            false)
        if (isServiceEnabled && !isServiceRunning())
        {
            val serviceIntent = Intent(this, StockInsiderService::class.java)
            startService(serviceIntent)
        }
    }




}
