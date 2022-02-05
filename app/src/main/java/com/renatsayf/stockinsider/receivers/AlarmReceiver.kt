package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.schedule.IScheduler
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.utils.AppCalendar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver @Inject constructor() : BroadcastReceiver()
{
    companion object
    {
        val TAG : String = this::class.java.name
    }

    @Inject
    lateinit var notification: ServiceNotification

    @Inject
    lateinit var appCalendar: AppCalendar

    @Inject
    lateinit var scheduler: IScheduler

    override fun onReceive(context: Context?, intent: Intent?)
    {
        if (context != null && intent != null)
        {
            if (intent.action == Scheduler.ONE_SHOOT_ACTION || intent.action == Scheduler.REPEAT_SHOOT_ACTION)
            {
                val setName = intent.getStringExtra(Scheduler.SET_NAME)
                when(true)
                {
                    true ->
                    {
                        val nextCalendar = appCalendar.getNextCalendar()
                        if (setName != null) {
                            //scheduler.scheduleOne(startTime = nextCalendar.timeInMillis, overTime = 0, setName = setName, requestCode = setName.hashCode())
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            println("***************** START ********************")
                            delay(20000)
                            withContext(Dispatchers.Main) {
                                ServiceNotification().createNotification(
                                    context,
                                    null,
                                    text = context.getString(R.string.base_notification_text)
                                ).show()
                            }
                            println("******************* END ********************")
                        }
                    }
                    else ->
                    {

                    }
                }
            }
        }
    }


}