package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import com.renatsayf.stockinsider.R

class BootCompletedReceiver : BroadcastReceiver()
{
    companion object
    {
        fun enable(context : Context)
        {
            val receiver = ComponentName(context, BootCompletedReceiver::class.java)
            context.packageManager.setComponentEnabledSetting(
                    receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                                                             )
        }
    }
    override fun onReceive(context : Context?, intent : Intent?)
    {
        val action = intent?.action
        if (action == "android.intent.action.BOOT_COMPLETED") {
            context?.let {

                val message = context.getString(R.string.app_name).plus(" is enabled")
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
        return
    }
}