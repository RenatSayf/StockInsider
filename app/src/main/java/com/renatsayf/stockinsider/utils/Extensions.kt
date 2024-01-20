@file:Suppress("UnnecessaryVariable", "UNUSED_DESTRUCTURED_PARAMETER_ENTRY")

package com.renatsayf.stockinsider.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.models.DateFormats
import com.renatsayf.stockinsider.models.Source
import com.renatsayf.stockinsider.receivers.HardwareButtonsReceiver
import com.renatsayf.stockinsider.service.WorkTask
import com.renatsayf.stockinsider.ui.dialogs.InfoDialog
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

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

inline fun <reified T : Parcelable> Bundle.getParcelableArrayListCompat(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayListCompat(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> {
        getParcelable(key)
    }
}

fun Context.haveWorkTask(): Boolean {
    val workManager = WorkManager.getInstance(this)
    val workInfos = workManager.getWorkInfosByTag(WorkTask.TAG)
    val infoList = workInfos.get().orEmpty()
    if (BuildConfig.DEBUG) println("*************** workList: $infoList ***************************")
    return infoList.any {
        it.state.name == "ENQUEUED"
    }
}

fun Fragment.haveWorkTask(): Boolean {
    return requireContext().haveWorkTask()
}

fun Context.startOneTimeBackgroundWork(startTime: Long): Operation {

    val workManager = WorkManager.getInstance(this)
    return run {
        val workRequest = WorkTask().createOneTimeTask(
            context = this,
            name = "${this.packageName}.${WorkTask::class.simpleName}.Task",
            startTime = startTime
        )
        workManager.enqueue(workRequest)
    }
}

fun Fragment.startOneTimeBackgroundWork(startTime: Long): Operation {
    return requireContext().startOneTimeBackgroundWork(startTime)
}

fun Context.cancelBackgroundWork(): LiveData<Operation.State> {
    val operation = WorkManager.getInstance(this).cancelAllWorkByTag(WorkTask.TAG)
    val result = operation.state
    return operation.state
}

fun Fragment.cancelBackgroundWork(): LiveData<Operation.State> {
    return requireActivity().cancelBackgroundWork()
}

fun Activity.doShare()
{
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    val shareBody = "http://play.google.com/store/apps/details?id=" + this.packageName
    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
    startActivity(Intent.createChooser(sharingIntent, getString(R.string.text_share_using)))
}

val Context.appPref: SharedPreferences
    get() {
        return this.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)
    }

val Fragment.appPref: SharedPreferences
    get() {
        return requireContext().appPref
    }

//region Hint Checking_internet_connection
fun Context.isNetworkAvailable(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
            when {
                capabilities != null -> {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        else -> capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    }
                }
                else -> false
            }
        }
        else -> {
            try {
                val activeNetworkInfo = manager.activeNetworkInfo
                activeNetworkInfo != null && activeNetworkInfo.isConnected
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                false
            }
        }
    }
}

fun Fragment.isNetworkAvailable(): Boolean {
    return requireContext().isNetworkAvailable()
}
//endregion Checking_internet_connection

fun View.setPopUpMenu(menuResource: Int): PopupMenu {
    return PopupMenu(this.context, this).apply {
        inflate(menuResource)
    }
}

fun Activity.startBrowserSearch(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    this.startActivity(intent)
}

fun Fragment.startBrowserSearch(url: String) {
    requireActivity().startBrowserSearch(url)
}

fun Context.hideKeyBoard(view: View) {
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.hideKeyBoard(view: View) {
    requireContext().hideKeyBoard(view)
}

fun String.convertDefaultWithoutTime(): String? {
    val format = "yyyy-MM-dd"
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    val parsedDate = dateFormat.parse(this)
    val dayMonthFormat = SimpleDateFormat(format, Locale.getDefault())
    return parsedDate?.let { dayMonthFormat.format(parsedDate) }
}

fun AppCompatActivity.showInfoDialog(
    title: String = "",
    message: String = "",
    status: InfoDialog.DialogStatus = InfoDialog.DialogStatus.INFO,
    callback: (Int) -> Unit = {}
) {
    val dialog = InfoDialog.newInstance(title, message, status, callback)
    dialog.show(this.supportFragmentManager, InfoDialog.TAG)
}

fun Fragment.showInfoDialog(
    title: String = "",
    message: String = "",
    status: InfoDialog.DialogStatus = InfoDialog.DialogStatus.INFO,
    callback: (Int) -> Unit = {}
) {
    (requireActivity() as AppCompatActivity).showInfoDialog(title, message, status, callback)
}

fun <V, T> Map<V, List<T>>.getValuesSize(): Int {
    var size = 0
    this.forEach { (t, u) ->
        size += u.size
    }
    return size
}

fun Activity.openAppSystemSettings(action: String = Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
    startActivity(Intent().apply {
        this.action = action
        data = Uri.fromParts("package", this@openAppSystemSettings.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

fun Fragment.openAppSystemSettings(action: String = Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
    requireActivity().openAppSystemSettings(action)
}

fun Long.timeToFormattedString(): String =
    SimpleDateFormat(DateFormats.DEFAULT_FORMAT.format, Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }.format(this).replace("T", " ")

@Throws(Exception::class)
fun checkTestPort(): Boolean {
    return if (BuildConfig.DATA_SOURCE == Source.LOCALHOST.name) true
    else throw Exception("****************** Не тестовый порт. Выберите в меню Build Variants localhostDebug *************************")
}

fun Context.goToAppStore() {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(this.getString(R.string.app_link).plus(this.packageName))
    startActivity(intent)
}

fun String.printIfDebug() {
    if (BuildConfig.DEBUG) {
        println(this)
    }
}

fun Exception.throwIfDebug() {
    if (BuildConfig.DEBUG) {
        throw this
    }
}

fun Exception.printStackTraceIfDebug() {
    if (BuildConfig.DEBUG) {
        this.printStackTrace()
    }
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerHardWareReceiver(receiver: HardwareButtonsReceiver) {

    val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
    }
    else {
        this.registerReceiver(receiver, intentFilter)
    }
}



























