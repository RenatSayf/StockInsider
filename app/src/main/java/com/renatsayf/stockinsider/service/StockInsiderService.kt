package com.renatsayf.stockinsider.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.utils.*
import java.util.*
import javax.inject.Inject


class StockInsiderService : Service()
{
    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var notification: ServiceNotification

    @Inject
    lateinit var appCalendar: AppCalendar

    @Inject
    lateinit var appLog: AppLog

    private lateinit var receiver: PhoneUnlockedReceiver

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
        MainActivity.appComponent.inject(this)

        serviceEvent.value = Event(START_KEY)

        receiver = PhoneUnlockedReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(receiver, filter)

        val nextCalendar = appCalendar.getNextCalendar()
        val (time, alarmPendingIntent) = AlarmPendingIntent.create(this, nextCalendar)
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.apply {
            setExact(AlarmManager.RTC_WAKEUP, time, alarmPendingIntent)
        }
        val message = "Every day at ${utils.getFormattedDateTime(0, Date(time))} we will check new deals."
        appLog.print(ResultFragment.TAG, message)

        val actionIntent = Intent(Intent.ACTION_MAIN)
        actionIntent.setClass(this, MainActivity::class.java)
        actionIntent.flags = Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        val notificationPendingIntent = PendingIntent.getActivity(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val appNotification = notification.createNotification(
                this,
                notificationPendingIntent,
                message,
                R.drawable.ic_stock_hause_cold,
                R.color.colorGold
        )

        appNotification.notification?.let {
            startForeground(ServiceNotification.NOTIFICATION_ID, appNotification.notification)
            appLog.print(TAG, "Сервис запущен в ${utils.getFormattedDateTime(0, Date(System.currentTimeMillis()))}")
        }

    }

    override fun onLowMemory()
    {
        super.onLowMemory()
        notification.createNotification(
                this,
                null,
                this.getString(R.string.text_not_memory),
                R.drawable.ic_stock_hause_cold,
                R.color.colorRed
        ).show()
        appLog.print(TAG, this.getString(R.string.text_not_memory))
    }

    override fun onDestroy()
    {
        super.onDestroy()
        unregisterReceiver(receiver)
        serviceEvent.value = Event(STOP_KEY)
        AlarmPendingIntent.cancel(this)
        notification.cancelNotifications(this)
        appLog.print(TAG, "Service is closed...")
    }




    inner class PhoneUnlockedReceiver : BroadcastReceiver()
    {
        // TODO: обработчик событий нажатия кнопки блокировки, выключения экрана....
        override fun onReceive(context: Context?, intent: Intent?)
        {
            context?.let{
                if (intent != null)
                {
                    val isAlarmSettings = context.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)
                        .getBoolean(AlarmPendingIntent.IS_ALARM_SETUP_KEY, false)
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (intent.action == Intent.ACTION_SCREEN_ON)
                    {
                        appLog.print(TAG, "**********  Экран разблокирован  ***********************")
                        if (!AlarmPendingIntent.isAlarmSetup(context))
                        {
                            AlarmPendingIntent.create(context).let { result ->
                                alarmManager.apply {
                                    setExact(AlarmManager.RTC_WAKEUP, result.time, result.instance)
                                    val message = "**********  Alarm has been recreated  **************"
                                    appLog.print(TAG, message)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}