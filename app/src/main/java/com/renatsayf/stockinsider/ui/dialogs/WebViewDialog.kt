package com.renatsayf.stockinsider.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import kotlinx.android.synthetic.main.web_view_dialog.view.*

class WebViewDialog : DialogFragment()
{
    companion object
    {
        val TAG = this::class.java.canonicalName.plus(".tag")
    }

    private lateinit var dialogView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.web_view_dialog, ConstraintLayout(requireContext()), false)
        dialogView.apply {
            dialogWebView.loadUrl("file:///android_asset/user-agreement/index.html")
        }
        val builder = AlertDialog.Builder(requireContext()).apply {
            setView(dialogView)
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
            setOnCancelListener {
                requireActivity().finish()
                dismiss()
            }
        }
        return builder.create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        dialogView.dialogWebView.setOnTouchListener(object : View.OnTouchListener
        {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean
            {
                p0?.performClick()
                return false
            }
        })
    }

    override fun onDestroy()
    {
        super.onDestroy()
        val isAgree = requireContext().getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE).getBoolean(MainActivity.KEY_IS_AGREE, true)
        if (!isAgree) requireActivity().finish()
    }
}