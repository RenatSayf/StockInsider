package com.renatsayf.stockinsider.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.renatsayf.stockinsider.MainActivity

fun Fragment.askForPermission(
    permission: String,
    onInit: () -> Unit = {},
    onGranted: () -> Unit = {}
) {
    val isShould = this.shouldShowRequestPermissionRationale(permission)
    val selfPermission = ContextCompat.checkSelfPermission(requireContext(), permission)
    if (selfPermission == PackageManager.PERMISSION_GRANTED) {
        onGranted.invoke()
    }
    else if (selfPermission == PackageManager.PERMISSION_DENIED || isShould) {
        onInit.invoke()
    }
}

private val Context.appPref: SharedPreferences
    get() {
        return this.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE)
    }

var Context.isAdsDisabled: Boolean
    get() {
        return this.appPref.getBoolean(KEY_IS_AD_DISABLED, false)
    }
    set(value) {
        this.appPref.edit().putBoolean(KEY_IS_AD_DISABLED, value).apply()
    }