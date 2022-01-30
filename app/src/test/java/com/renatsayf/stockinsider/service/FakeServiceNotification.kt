package com.renatsayf.stockinsider.service

import android.content.Context
import com.renatsayf.stockinsider.models.Deal

object FakeServiceNotification {
    val notify: (Context, ArrayList<Deal>) -> Unit = { context: Context, list: ArrayList<Deal> ->

        println(context.packageName)
        if (list.isNotEmpty()) {
            println(list[0].toString())
        } else {
            println("********** No data *************")
        }
    }
}