package com.renatsayf.stockinsider.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.service.WorkTask
import com.renatsayf.stockinsider.ui.tracking.list.TrackingListFragment
import java.io.Serializable

const val KEY_FRAGMENT_RESULT = "KEY_FRAGMENT_RESULT"


fun Context.getStringFromFile(fileName: String): String {
    val inputStream = this.assets.open(fileName)
    val size = inputStream.available()
    val buffer = ByteArray(size)
    inputStream.read(buffer)
    inputStream.close()
    return String(buffer)
}

fun View.showSnackBar(
    message: String,
    length: Int = Snackbar.LENGTH_SHORT
) {
    Snackbar.make(this, message, length).show()
}

fun Fragment.showSnackBar(
    message: String,
    length: Int = Snackbar.LENGTH_SHORT
) {
    requireView().showSnackBar(message, length)
}

fun sortByAnotherList(targetList: List<Company>, anotherList: List<String>): List<Company> {

    val targetCompanyList = targetList.toMutableList()
    val targetTickerList = targetList.toMutableList().map {
        it.ticker
    }
    anotherList.forEach {
        if (!targetTickerList.contains(it)) {
            targetCompanyList.add(Company(it, "Unnamed company"))
        }
    }
    val anotherMap = anotherList.withIndex().associate {
        it.value to it.index
    }
    val sortedList = targetCompanyList.sortedBy {
        anotherMap[it.ticker]
    }
    return sortedList
}

fun View.setVisible(flag: Boolean, invisibleMode: Int = -1) {
    when {
        !flag && invisibleMode < 0 -> {
            this.visibility = View.GONE
        }
        !flag && invisibleMode > 0 -> {
            this.visibility = View.INVISIBLE
        }
        flag -> this.visibility = View.VISIBLE
    }
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

@Suppress("UNCHECKED_CAST")
fun <T : Serializable?> Bundle.getSerializableCompat(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, clazz)
    } else (getSerializable(key) as? T)
}

fun Activity.startBackgroundWork() {
    val workRequest = WorkTask.createPeriodicTask(this, TrackingListFragment.TASK_NAME)
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        TrackingListFragment.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}

fun Activity.cancelBackgroundWork() {
    WorkManager.getInstance(this).cancelAllWorkByTag(WorkTask.TAG)
}

fun Fragment.startBackgroundWork() {
    requireActivity().startBackgroundWork()
}

fun Fragment.cancelBackgroundWork() {
    requireActivity().cancelBackgroundWork()
}



























