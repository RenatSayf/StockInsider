package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HardwareButtonsReceiver : BroadcastReceiver() {

    private var triggerCount = 0
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context is Context && intent is Intent && intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            val reason = intent.getStringExtra("reason")
            if (reason != null) {
                when {
                    reason == "recentapps" && triggerCount == 1 -> {

                    }
                    reason == "homekey" && triggerCount == 1 -> {

                    }
                }
            }
        }
    }
}