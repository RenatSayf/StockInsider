package com.renatsayf.stockinsider

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.hypertrack.hyperlog.HyperLog
import com.renatsayf.stockinsider.di.AppComponent
import com.renatsayf.stockinsider.di.DaggerAppComponent
import com.renatsayf.stockinsider.di.modules.RoomDataBaseModule
import com.renatsayf.stockinsider.models.DataTransferModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.service.ServiceTask
import com.renatsayf.stockinsider.service.StockInsiderService
import kotlinx.android.synthetic.main.load_progress_layout.*
import javax.inject.Inject

class MainActivity @Inject constructor() : AppCompatActivity(), StockInsiderService.IShowMessage {

    companion object
    {
        lateinit var appComponent : AppComponent
        const val IS_ALARM_KEY = "com.renatsayf.stockinsider.is_alarm_key"
    }

    private lateinit var appBarConfiguration : AppBarConfiguration
    private lateinit var dataTransferModel : DataTransferModel

    @Inject
    lateinit var notification : ServiceNotification

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appComponent = DaggerAppComponent.builder()
            .roomDataBaseModule(RoomDataBaseModule(this))
            .build()

        HyperLog.initialize(this)
        HyperLog.setLogLevel(Log.VERBOSE)
        //HyperLog.getDeviceLogsInFile(this)

        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        appComponent.inject(this)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.nav_home, R.id.nav_strategy, R.id.nav_exit
                     ), drawerLayout
                                                 )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        loadProgreesBar.visibility = View.GONE

        dataTransferModel = this.run {
            ViewModelProvider(this)[DataTransferModel::class.java]
        }
        val dealList = intent?.getParcelableArrayListExtra<Deal>(ServiceTask.KEY_DEAL_LIST)
        println("${this.javaClass.simpleName}: dealList = ${dealList?.size}")
        dealList?.let {
            dataTransferModel.setDealList(dealList)
            navController.navigate(R.id.resultFragment, null)
        }
        if (isServiceRunning())
        {
            stopService(Intent(this, StockInsiderService::class.java))
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
                val deviceLogsFile = HyperLog.getDeviceLogsInFile(this)
                return true
            }
        }
        return true
    }

    override fun onSupportNavigateUp() : Boolean
    {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        with(this.getPreferences(Context.MODE_PRIVATE))
        {
            when(this?.getBoolean(StockInsiderService.PREFERENCE_KEY, false))
            {
                true ->
                {
                    val serviceIntent = Intent(this@MainActivity, StockInsiderService()::class.java)
                    StockInsiderService.setMessageListener(this@MainActivity)
                    this@MainActivity.startService(serviceIntent)
                }
                else -> return@with
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

    override fun showMessage(text: String)
    {

    }



}
