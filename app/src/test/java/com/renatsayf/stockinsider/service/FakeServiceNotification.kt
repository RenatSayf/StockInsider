package com.renatsayf.stockinsider.service

import android.content.Context
import com.renatsayf.stockinsider.db.RoomSearchSet

object FakeServiceNotification {
    val notify: (Context, Int, RoomSearchSet) -> Unit = { context: Context, count, set ->

        println(context.packageName)
        if (count > 0) {
            println(set.toString())
        } else {
            println("********** No data *************")
        }
    }
}