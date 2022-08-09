package com.renatsayf.stockinsider.utils

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar


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