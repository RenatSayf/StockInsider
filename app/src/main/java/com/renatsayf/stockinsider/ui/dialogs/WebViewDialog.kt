@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.WebViewDialogBinding

class WebViewDialog : DialogFragment()
{
    companion object
    {
        val TAG = this::class.java.simpleName.plus(".tag")
    }

    private lateinit var binding: WebViewDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        binding = WebViewDialogBinding.inflate(layoutInflater)

        binding.dialogWebView.loadUrl("file:///android_asset/user-agreement/index.html")

        val builder = AlertDialog.Builder(requireContext()).apply {
            setView(binding.root)
            setTitle(getString(R.string.text_user_agreement))
            setPositiveButton(getString(R.string.text_accept), object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    requireContext().getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE).edit().putBoolean(MainActivity.KEY_IS_AGREE, true).apply()
                    dismiss()
                }
            })
            setNegativeButton(getString(R.string.text_cancel), object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, p1: Int)
                {
                    requireActivity().finish()
                    dismiss()
                }
            })
        }
        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface) {

        requireActivity().finish()
        dismiss()
        super.onCancel(dialog)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        val isAgree = requireContext().getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE).getBoolean(MainActivity.KEY_IS_AGREE, true)
        if (!isAgree) requireActivity().finish()
    }
}