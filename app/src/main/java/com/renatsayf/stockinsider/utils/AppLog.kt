package com.renatsayf.stockinsider.utils

import android.content.Context
import android.util.Log
import com.hypertrack.hyperlog.HyperLog
import javax.inject.Inject

class AppLog @Inject constructor(private val context: Context)
{
    companion object
    {
        var isLog: Boolean = true
    }

    init
    {
        HyperLog.initialize(context)
        HyperLog.setLogLevel(Log.VERBOSE)
    }

    fun print(tag: String, message: String)
    {
        if (isLog)
        {
            HyperLog.d(tag, message)
        }
    }
}