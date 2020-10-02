package com.renatsayf.stockinsider

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
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
import com.renatsayf.stockinsider.models.DataTransferModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.adapters.ExpandableMenuAdapter
import com.renatsayf.stockinsider.ui.donate.DonateDialog
import com.renatsayf.stockinsider.ui.latest.purchases.Purchases1Fragment
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
    }

    lateinit var navController: NavController
    private lateinit var appBarConfiguration : AppBarConfiguration
    private lateinit var dataTransferModel : DataTransferModel
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var ad: InterstitialAd

    @Inject
    lateinit var notification : ServiceNotification

    @Inject
    lateinit var appLog: AppLog

    @Inject
    lateinit var donateDialog: DonateDialog

    override fun onCreate(savedInstanceState : Bundle?)
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

        loadProgreesBar.visibility = View.GONE

        dataTransferModel = this.run {
            ViewModelProvider(this)[DataTransferModel::class.java]
        }
        val dealList = intent?.getParcelableArrayListExtra<Deal>(Deal.KEY_DEAL_LIST)
        dealList?.let {
            dataTransferModel.setDealList(dealList)
            navController.navigate(R.id.nav_result, null)
        }

        val expandableMenuAdapter = ExpandableMenuAdapter(this)
        expandMenu.apply {
            setAdapter(expandableMenuAdapter)
            setOnGroupClickListener(object : ExpandableListView.OnGroupClickListener{
                override fun onGroupClick(
                        p0: ExpandableListView?,
                        p1: View?,
                        p2: Int,
                        p3: Long
                ): Boolean
                {
                    when(p2)
                    {
                        0 ->
                        {
                            navController.navigate(R.id.nav_home)
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        2 ->
                        {
                            navController.navigate(R.id.nav_strategy)
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        4 ->
                        {
                            drawerLayout.closeDrawer(GravityCompat.START)
                            finish()
                        }
                    }
                    return false
                }
            })
            setOnChildClickListener(object : ExpandableListView.OnChildClickListener{
                override fun onChildClick(
                        p0: ExpandableListView?,
                        p1: View?,
                        p2: Int,
                        p3: Int,
                        p4: Long
                ): Boolean
                {
                    when
                    {
                        p2 == 1 && p3 == 0 ->
                        {
                            val bundle = Bundle().apply {
                                putString(Purchases1Fragment.ARG_SET_NAME, Purchases1Fragment.PURCHASES_1_FOR_WEEK)
                            }
                            navController.navigate(R.id.nav_purchases, bundle)
                        }
                        p2 == 1 && p3 == 1 ->
                        {
                            val bundle = Bundle().apply {
                                putString(Purchases1Fragment.ARG_SET_NAME, Purchases1Fragment.PURCHASES_5_FOR_WEEK)
                            }
                            navController.navigate(R.id.nav_purchases, bundle)
                            //navController.navigate(R.id.nav_purchases25)
                        }
                        p2 == 3 && p3 == 0 ->
                        {
                            donateDialog.show(supportFragmentManager, DonateDialog.TAG)
                        }
                        p2 == 3 && p3 == 1 ->
                        {
                            if (ad.isLoaded)
                            {
                                ad.show()
                            }
                        }
                    }
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return false
                }
            })
        }

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

    override fun onCreateOptionsMenu(menu : Menu) : Boolean
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

    fun getFilingOrTradeValue(position : Int) : String
    {
        val filingValue : String
        return try
        {
            val filingValues = this.resources?.getIntArray(R.array.value_for_filing_date)
            filingValue = filingValues?.get(position).toString()
            filingValue
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            ""
        }
    }

    fun getGroupingValue(position : Int) : String
    {
        val groupingValue : String
        return try
        {
            val groupingValues = this.resources?.getIntArray(R.array.value_for_grouping)
            groupingValue = groupingValues?.get(position).toString()
            groupingValue
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            ""
        }
    }

    fun getSortingValue(position : Int) : String
    {
        val sortingValue : String
        return try
        {
            val sortingValues = this.resources?.getIntArray(R.array.value_for_sorting)
            sortingValue = sortingValues?.get(position).toString()
            sortingValue
        }
        catch (e : Exception)
        {
            e.printStackTrace()
            ""
        }
    }

    fun getCheckBoxValue(checkBox : CheckBox) : String
    {
        val id = checkBox.id
        appLog.print(this::class.java.canonicalName.toString(), "*************** checkBox.id = ${id} **********************")
        return if(checkBox.isChecked && id == R.id.officer_CheBox)
        {
            "1&iscob=1&isceo=1&ispres=1&iscoo=1&iscfo=1&isgc=1&isvp=1"
        }
        else if (checkBox.isChecked && id != R.id.officer_CheBox)
        {
            "1"
        }

        else
        {
            ""
        }
    }

    fun getTickersString(string : String) : String
    {
        val pattern = Regex("\\s")
        return string.trim().replace(pattern, "+")
    }

    fun hideKeyBoard(view : View)
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
                this.packageName -> {
                    return true
                }
            }
        }
        return false
    }

    override fun finish()
    {
        super.finish()
        val isServiceEnabled = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE).getBoolean(AlarmPendingIntent.IS_ALARM_SETUP_KEY, false)
        if (isServiceEnabled && !isServiceRunning())
        {
            val serviceIntent = Intent(this, StockInsiderService::class.java)
            startService(serviceIntent)
        }
    }


}
