@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.WebViewDialogBinding
import com.renatsayf.stockinsider.utils.appPref

class WebViewDialog : DialogFragment() {

    companion object {
        val TAG = this::class.java.simpleName.plus(".tag")

        fun getInstance(): WebViewDialog {
            return WebViewDialog()
        }
    }

    private lateinit var binding: WebViewDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = WebViewDialogBinding.inflate(layoutInflater)

        binding.dialogWebView.apply {
            val currentLocale = ConfigurationCompat.getLocales(resources.configuration)[0]
            val language = currentLocale?.language
            if (language == "ru") {
                loadUrl("file:///android_asset/user-agreement/index.html")
            }
            else {
                loadUrl("file:///android_asset/user-agreement/index-en.html")
            }
        }

        val builder = AlertDialog.Builder(requireContext()).apply {
            setView(binding.root)
            setTitle(getString(R.string.text_user_agreement))
            setPositiveButton(
                getString(R.string.text_accept),
                object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        appPref.edit().putBoolean(MainActivity.KEY_IS_AGREE, true).apply()
                        dismiss()
                    }
                })
            setNegativeButton(
                getString(R.string.text_cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        requireActivity().finish()
                        dismiss()
                    }
                })
        }
        return builder.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {

        requireActivity().finish()
        dismiss()
        super.onCancel(dialog)
    }

    override fun onDestroy() {
        super.onDestroy()
        val isAgree = appPref.getBoolean(MainActivity.KEY_IS_AGREE, true)
        if (!isAgree) requireActivity().finish()
    }
}