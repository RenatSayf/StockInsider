package com.renatsayf.stockinsider.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.icu.util.TimeZone
import android.os.IBinder
import com.hypertrack.hyperlog.HyperLog
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.utils.IsFilingTime
import com.renatsayf.stockinsider.utils.Utils
import java.util.*
import javax.inject.Inject

class StockInsiderService : Service()
{
    @Inject
    lateinit var utils : Utils

    @Inject
    lateinit var notification : ServiceNotification

    @Inject
    lateinit var searchRequest : SearchRequest

    private lateinit var timer: Timer

    companion object
    {
        val TAG = this::class.java.simpleName
        const val PREFERENCE_KEY = "com.renatsayf.stockinsider.service.StockInsiderService"
        var isStopService = false
        var iShowMessage : IShowMessage? = null
        fun setMessageListener(iMessageListener : IShowMessage)
        {
            iShowMessage = iMessageListener
        }
    }

    interface IShowMessage
    {
        fun showMessage(text : String)
    }

    override fun onBind(p0 : Intent?) : IBinder?
    {
        return null
    }

    override fun onStartCommand(intent : Intent?, flags : Int, startId : Int) : Int
    {
        return START_STICKY
    }

    override fun onCreate()
    {
        super.onCreate()
        MainActivity.appComponent.inject(this)

        val intent = Intent(Intent.ACTION_MAIN)
        intent.setClass(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val appNotification = notification.createNotification(this, pendingIntent, getString(R.string.base_notification_text), R.drawable.ic_stock_hause_cold, R.color.colorGold)

        appNotification.notification?.let{
            startForeground(ServiceNotification.NOTIFICATION_ID, appNotification.notification)
            HyperLog.d(TAG, "Сервис запущен в ${utils.getFormattedDateTime(0, Date(System.currentTimeMillis()))}")
        }

        val timeZone = TimeZone.getTimeZone(this.getString(R.string.app_time_zone))
        timer = Timer("appTimer")
        timer.scheduleAtFixedRate(ServiceTask(this, timeZone), IsFilingTime.START_DELAY, IsFilingTime.LOAD_INTERVAL)
    }

    override fun onLowMemory()
    {
        super.onLowMemory()
        isStopService = true
        notification.createNotification(this, null, this.getString(R.string.text_not_memory), R.drawable.ic_stock_hause_cold, R.color.colorRed).show()
        HyperLog.d(TAG, this.getString(R.string.text_not_memory))
    }

    override fun onDestroy()
    {
        super.onDestroy()
        timer.cancel()
        timer.purge()
        when(iShowMessage)
        {
            null -> throw Throwable("${this.javaClass.simpleName} setMessageListener() not enabled ")
            else ->
            {
                notification.cancelNotifications(this)
                iShowMessage?.showMessage(this.getString(R.string.text_search_is_disabled))
            }
        }
        //notification.createNotification(this, null, "Service is closed", R.drawable.ic_stock_hause_cold, R.color.colorRed).show(3333)
        HyperLog.d(TAG, "Service is closed...")
    }


}