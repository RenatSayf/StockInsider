package com.renatsayf.stockinsider.service

import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R

class StockInsiderService : Service()
{
    private val serviceId : Int = this.hashCode()
    private val chanelId: String = "channel_com.renatsayf.stockinsider.network"

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

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT)

        val notification = NotificationCompat.Builder(this, chanelId)
            .setSmallIcon(R.drawable.ic_menu_send)
            .setContentTitle(this.getString(R.string.app_name))
            .setContentText("Press to open the app")
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent).build()

        startForeground(serviceId, notification)
    }

    override fun onLowMemory()
    {
        super.onLowMemory()

    }

    override fun startService(service : Intent?) : ComponentName?
    {
        return super.startService(service)
    }
}