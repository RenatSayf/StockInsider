package com.renatsayf.stockinsider.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.TimeZone
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.receivers.AlarmReceiver
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit


class StockInsiderService : Service()
{
    companion object
    {
        val TAG = this::class.java.simpleName
        val START_KEY = TAG.plus("start")
        val STOP_KEY = TAG.plus("stop")
        val serviceEvent : MutableLiveData<Event<String>> = MutableLiveData()
    }

    override fun onBind(p0: Intent?): IBinder?
    {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        return START_STICKY
    }

    override fun onCreate()
    {
        super.onCreate()

        val appNotification = ServiceNotification().createNotification(
                context = this,
                null,
                text = getString(R.string.base_notification_text)
        )

        appNotification.notification?.let {
            startForeground(ServiceNotification.NOTIFICATION_ID, appNotification.notification)
        }

        CoroutineScope(Dispatchers.Default).launch {
            delay(10000L)
            stopForeground(true)
        }

    }

    override fun onLowMemory()
    {
        super.onLowMemory()
        ServiceNotification().createNotification(
                this,
                null,
                this.getString(R.string.text_not_memory),
                R.drawable.ic_stock_hause_cold
        ).show()
    }




//    inner class PhoneUnlockedReceiver : BroadcastReceiver()
//    {
//
//        // TODO: обработчик событий нажатия кнопки блокировки, выключения экрана....
//        override fun onReceive(context: Context?, intent: Intent?)
//        {
//            context?.let{
//                if (intent != null)
//                {
//                    val isAlarmSetting = context.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)
//                        .getBoolean(AlarmPendingIntent.IS_ALARM_SETUP_KEY, false)
//                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//                    if (intent.action == Intent.ACTION_SCREEN_ON)
//                    {
//                        if (!AlarmPendingIntent.isAlarmSetup(context) && isAlarmSetting)
//                        {
//                            AlarmPendingIntent.create(context).let { result ->
//                                alarmManager.apply {
//                                    setExact(AlarmManager.RTC_WAKEUP, result.time, result.instance)
//                                    val message = "**********  Alarm has been recreated  **************"
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}