package com.renatsayf.stockinsider.utils

import android.content.Context
import android.util.Log
import com.hypertrack.hyperlog.HyperLog
import java.io.File
import javax.inject.Inject

class AppLog(private val context: Context)
{
    companion object
    {
       private const val isLog: Boolean = true //TODO to release version switch to false
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
        when
        {
            isLog ->
            {
                HyperLog.d(tag, message)
            }
        }
    }

    fun getDeviceLogsInFile() : File?
    {
        return when(isLog)
        {
            true -> HyperLog.getDeviceLogsInFile(context)
            else -> null
        }
    }
}