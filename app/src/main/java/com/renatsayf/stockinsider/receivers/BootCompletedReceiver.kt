package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import javax.inject.Inject

class BootCompletedReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context?, intent: Intent?)
    {
        val action = intent?.action
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
            action.equals("android.intent.action.QUICKBOOT_POWERON") ||
            action.equals("com.htc.intent.action.QUICKBOOT_POWERON") ||
            action.equals(Intent.ACTION_PACKAGE_REPLACED) ||
            action.equals(Intent.ACTION_PACKAGE_ADDED)
        )
        {
            context?.let { c ->
               val isAlarmSetup = c.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)
                   .getBoolean(AlarmPendingIntent.IS_ALARM_SETUP_KEY, false)
                when(isAlarmSetup)
                {
                    true ->
                    {
                        val seviceIntent = Intent(c, StockInsiderService::class.java)
                        c.startService(seviceIntent)
                    }
                    else -> return
                }
            }
        }
        return
    }
}