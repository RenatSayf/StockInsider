package com.renatsayf.stockinsider

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.load_progress_layout.*

class MainActivity : AppCompatActivity()
{

    private lateinit var appBarConfiguration : AppBarConfiguration

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.nav_home, R.id.nav_tools, R.id.nav_share
                     ), drawerLayout
                                                 )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        loadProgreesBar.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu : Menu) : Boolean
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp() : Boolean
    {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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


}
