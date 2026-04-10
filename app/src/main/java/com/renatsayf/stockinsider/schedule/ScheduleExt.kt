package com.renatsayf.stockinsider.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.renatsayf.stockinsider.receivers.AlarmReceiver
import com.renatsayf.stockinsider.utils.printStackTraceIfDebug
import java.util.concurrent.TimeUnit

private const val REMINDER_CODE = 589745

private var alarmTime = MutableLiveData<Long>(0)
val triggerTime: LiveData<Long> = alarmTime

fun Context.setReminderAlarm(minutesFromNow: Long): Long {
    val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(this, AlarmReceiver::class.java)

    val pendingIntent = PendingIntent.getBroadcast(
        this,
        REMINDER_CODE,
        intent,
        PendingIntent.FLAG_IMMUTABLE // Важно для Android 12+
    )

    val triggerTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutesFromNow)

    try {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
        alarmTime.value = triggerTime
        return triggerTime
    } catch (e: SecurityException) {
        e.printStackTraceIfDebug()
        return 0
    }
}

fun Context.cancelReminderAlarm() {
    val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // 1. Creating EXACTLY THE SAME Intent as when creating
    val intent = Intent(this, AlarmReceiver::class.java)

    // 2. Creating a PendingIntent with the same ID (the second parameter)
    // The FLAG_NO_CREATE flag will tell the system: "don't create a new one if there isn't one"
    val pendingIntent = PendingIntent.getBroadcast(
        this,
        REMINDER_CODE, // ID должен совпадать с тем, что был при установке!
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
    )

    // 3. If such a PendingIntent is found in the system, we delete it.
    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel() // It is also recommended to cancel the PendingIntent itself.
    }
}

fun Context.isReminderAlarmActive(): Boolean {
    val intent = Intent(this, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        this,
        REMINDER_CODE,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    return pendingIntent != null // Returns true if the alarm clock is "waiting" in the system.
}