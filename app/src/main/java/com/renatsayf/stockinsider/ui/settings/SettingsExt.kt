package com.renatsayf.stockinsider.ui.settings

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

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