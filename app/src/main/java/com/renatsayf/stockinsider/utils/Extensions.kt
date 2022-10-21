package com.renatsayf.stockinsider.utils

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.repository.DataRepositoryImpl


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

fun sortByAnotherList(targetList: List<Companies>, anotherList: List<String>): List<Companies> {

    val targetCompanyList = targetList.toMutableList()
    val targetTickerList = targetList.toMutableList().map {
        it.ticker
    }
    anotherList.forEach {
        if (!targetTickerList.contains(it)) {
            targetCompanyList.add(Companies(it, "Unnamed company"))
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