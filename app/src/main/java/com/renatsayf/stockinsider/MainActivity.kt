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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ExpandableListView
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
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.ExpandableMenuAdapter
import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import com.renatsayf.stockinsider.ui.dialogs.SearchListDialog
import com.renatsayf.stockinsider.ui.dialogs.WebViewDialog
import com.renatsayf.stockinsider.ui.donate.DonateDialog
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.ui.strategy.AppDialog
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.AppLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.load_progress_layout.*
import javax.inject.Inject

@AndroidEntryPoint //TODO Hilt step 6
class MainActivity : AppCompatActivity()
{
    companion object
    {
        val APP_SETTINGS = "${this::class.java.`package`}.app_settings"
        val KEY_NO_SHOW_AGAIN = this::class.java.canonicalName.plus("key_no_show_again")
    }

    lateinit var navController: NavController
    private lateinit var appBarConfiguration : AppBarConfiguration
    private lateinit var appDialogListener : AppDialog.EventListener
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var ad: InterstitialAd

    @Inject
    lateinit var notification : ServiceNotification

    @Inject
    lateinit var appLog: AppLog

    @Inject
    lateinit var donateDialog: DonateDialog

    @Inject
    lateinit var confirmationDialog: ConfirmationDialog

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.AppTheme_NoActionBar)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView : NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.nav_home, R.id.nav_strategy
                ), drawerLayout
        )
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
        getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_NO_SHOW_AGAIN, false)
            apply()
        }
        //endregion
        loadProgreesBar.visibility = View.GONE

        appDialogListener = ViewModelProvider(this)[AppDialog.EventListener::class.java]

        val expandableMenuAdapter = ExpandableMenuAdapter(this)
        expandMenu.apply {
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
                                    AppDialog.getInstance("show_strategy", spannableMessage, context.getString(R.string.text_read), context.getString(R.string.text_close), context.getString(R.string.text_not_show_again))
                                        .show(supportFragmentManager.beginTransaction(), AppDialog.TAG)
                                }
                            }
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        6 ->
                        {
                            WebViewDialog().show(supportFragmentManager, "dsfdfdf")
                        }
                        7 ->
                        {
                            drawerLayout.closeDrawer(GravityCompat.START)
                            finish()
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
                            Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "pur_more1_for_3")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_pur_more1_for_3))
                            }.run { navController.navigate(R.id.nav_result, this) }
                        }
                        p2 == 1 && p3 == 1 ->
                        {
                            Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "pur_more5_for_3")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_pur_more5_for_3))
                            }.run { navController.navigate(R.id.nav_result, this) }
                        }
                        p2 == 1 && p3 == 2 ->
                        {
                            Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "sale_more1_for_3")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sale_more1_for_3))
                            }.run { navController.navigate(R.id.nav_result, this) }
                        }
                        p2 == 1 && p3 == 3 ->
                        {
                            Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "sale_more5_for_3")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sale_more5_for_3))
                            }.run { navController.navigate(R.id.nav_result, this) }
                        }
                        p2 == 2 && p3 == 0 ->
                        {
                            val bundle = Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "purchases_more_1")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_purchases_more_1))
                            }
                            navController.navigate(R.id.nav_result, bundle)
                        }
                        p2 == 2 && p3 == 1 ->
                        {
                            val bundle = Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "purchases_more_5")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_purchases_more_5))
                            }
                            navController.navigate(R.id.nav_result, bundle)
                        }
                        p2 == 2 && p3 == 2 ->
                        {
                            Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "sales_more_1")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sales_more_1))
                            }.run { navController.navigate(R.id.nav_result, this) }
                        }
                        p2 == 2 && p3 == 3 ->
                        {
                            Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "sales_more_5")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sales_more_5))
                            }.run {
                                navController.navigate(R.id.nav_result, this)
                            }
                        }
                        p2 == 3 && p3 == 0 ->
                        {
                            Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "pur_more1_for_14")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_pur_more1_for_14))
                            }.run { navController.navigate(R.id.nav_result, this) }
                        }
                        p2 == 3 && p3 == 1 ->
                        {
                            Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "pur_more5_for_14")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_pur_more5_for_14))
                            }.run { navController.navigate(R.id.nav_result, this) }
                        }
                        p2 == 3 && p3 == 2 ->
                        {
                            Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "sale_more1_for_14")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sale_more1_for_14))
                            }.run { navController.navigate(R.id.nav_result, this) }
                        }
                        p2 == 3 && p3 == 3 ->
                        {
                            Bundle().apply {
                                putString(ResultFragment.ARG_QUERY_NAME, "sale_more5_for_14")
                                putString(ResultFragment.ARG_TITLE, context.getString(R.string.text_sale_more5_for_14))
                            }.run { navController.navigate(R.id.nav_result, this) }
                        }
                        p2 == 5 && p3 == 0 ->
                        {
                            if (isNetworkConnectivity())
                            {
                                donateDialog.show(supportFragmentManager, DonateDialog.TAG)
                            }
                        }
                        p2 == 5 && p3 == 1 ->
                        {
                            if (isNetworkConnectivity())
                            {
                                if (ad.isLoaded)
                                {
                                    ad.show()
                                }
                                else
                                {
                                    ad.loadAd(AdRequest.Builder().build())
                                }
                            }
                        }
                    }
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return false
                }
            })
        }

        appDialogListener.data.observe(this, { event ->
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

        MobileAds.initialize(this){}
        ad = InterstitialAd(this).apply {
            adUnitId = if (BuildConfig.DEBUG)
            {
                getString(R.string.test_interstitial_ads_id)
            }
            else
            {
                getString(R.string.release_interstitial_ads_id)
            }
            loadAd(AdRequest.Builder().build())
            adListener = object : AdListener()
            {
                override fun onAdClosed() {
                    ad.loadAd(AdRequest.Builder().build())
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
            setSpan(
                    ForegroundColorSpan(Color.GREEN),
                    0,
                    string1.length - 1,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            setSpan(RelativeSizeSpan(2f), 0, string1.length - 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        }
        val spannableStringBuilder = SpannableStringBuilder(spannable1)

        val string2 = getString(R.string.text_strategy_dialog_1)+"\n"

        spannableStringBuilder.append(SpannableString(string2).apply
        {
            setSpan(UnderlineSpan(), 71, 85, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(
                    ForegroundColorSpan(getColor(R.color.colorSectionDivider)),
                    71,
                    85,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            setSpan(clickable, 71, 85, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        })

        val string3 = "\n"+getString(R.string.text_strategy_dialog_2)
        spannableStringBuilder.append(SpannableString(string3))
        return spannableStringBuilder
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId)
        {
            R.id.action_log_file ->
            {
                appLog.getDeviceLogsInFile()
                return true
            }
            R.id.nav_home ->
            {
                drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            R.id.action_my_search ->
            {
                SearchListDialog().show(supportFragmentManager, SearchListDialog.TAG)
            }
            R.id.action_settings ->
            {

            }
        }
        return super.onOptionsItemSelected(item)
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

    fun getFilingOrTradeValue(position: Int) : String
    {
        val filingValue : String
        return try
        {
            val filingValues = this.resources?.getIntArray(R.array.value_for_filing_date)
            filingValue = filingValues?.get(position).toString()
            filingValue
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            ""
        }
    }

    fun getGroupingValue(position: Int) : String
    {
        val groupingValue : String
        return try
        {
            val groupingValues = this.resources?.getIntArray(R.array.value_for_grouping)
            groupingValue = groupingValues?.get(position).toString()
            groupingValue
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            ""
        }
    }

    fun getSortingValue(position: Int) : String
    {
        val sortingValue : String
        return try
        {
            val sortingValues = this.resources?.getIntArray(R.array.value_for_sorting)
            sortingValue = sortingValues?.get(position).toString()
            sortingValue
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            ""
        }
    }

    fun getOfficerValue(value: Boolean) : String
    {
        return if (value) "1&iscob=1&isceo=1&ispres=1&iscoo=1&iscfo=1&isgc=1&isvp=1" else ""
    }

    fun getCheckBoxValue(value: Boolean) : String
    {
        return if (value) "1" else ""
    }

    fun getTickersString(string: String) : String
    {
        val pattern = Regex("\\s")
        return string.trim().replace(pattern, "+")
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
                return(it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            }
        }
        Snackbar.make(expandMenu, getString(R.string.text_inet_not_connection), Snackbar.LENGTH_LONG).show()
        return false
    }

    override fun finish()
    {
        super.finish()
        val isServiceEnabled = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE).getBoolean(
                AlarmPendingIntent.IS_ALARM_SETUP_KEY,
                false
        )
        if (isServiceEnabled && !isServiceRunning())
        {
            val serviceIntent = Intent(this, StockInsiderService::class.java)
            startService(serviceIntent)
        }
    }




}
