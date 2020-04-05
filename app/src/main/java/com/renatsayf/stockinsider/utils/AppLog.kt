package com.renatsayf.stockinsider.utils

import android.content.Context
import android.util.Log
import com.hypertrack.hyperlog.HyperLog
import javax.inject.Inject

class AppLog @Inject constructor(context: Context)
{
    companion object
    {
       private const val isLog: Boolean = true
    }

    init
    {
        if (isLog)
        {
            HyperLog.initialize(context)
            HyperLog.setLogLevel(Log.VERBOSE)
        }
    }

    fun print(tag: String, message: String)
    {
        if (isLog)
        {
            HyperLog.d(tag, message)
        }
    }
}