@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.TickerLayoutBinding

class TickersView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = R.style.Platform_MaterialComponents,
    resources: Int = 0
) : ConstraintLayout(context, attrs, style, resources) {

    private var contentTextView: AutoCompleteTextView? = null
    private var showButtonView: Button? = null
    private var clearButtonView: AppCompatImageView? = null

    interface Listener {
        fun onShowAllClick(list: List<String>)
        fun onContentChanged(text: String)
        fun onContentCleared()
    }

    private var listener: Listener? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val view = View.inflate(context, R.layout.tickers_layout, this)

        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.TickersView)
        val titleText = attrsArray.getString(R.styleable.TickersView_titleText)
        val titleColor = attrsArray.getColor(R.styleable.TickersView_titleTextColor, Color.YELLOW)

        view.findViewById<TextView>(R.id.blockTitle).apply {
            text = titleText
            setTextColor(titleColor)
        }

        val contentText = attrsArray.getString(R.styleable.TickersView_contentText)
        val contentColor = attrsArray.getColor(R.styleable.TickersView_contentTextColor, Color.YELLOW)
        val hint = attrsArray.getString(R.styleable.TickersView_hint)
        val hintColor = attrsArray.getColor(R.styleable.TickersView_hintTextColor, Color.GRAY)

        var tickerText = ""
        contentTextView = view.findViewById<AutoCompleteTextView>(R.id.contentTextView).apply {
            setText(contentText)
            setTextColor(contentColor)
            setHint(hint)
            setHintTextColor(hintColor)

            doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrEmpty())
                {
                    val c = text[text.length - 1]
                    if (c.isWhitespace())
                    {
                        tickerText = text.toString()
                    }
                }
                else
                {
                    tickerText = ""
                }
                listener?.onContentChanged(text.toString())
            }
            onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(p0: AdapterView<*>?, v: View?, p2: Int, p3: Long) {
                    v?.let {
                        val tickerLayout = TickerLayoutBinding.bind(v)
                        val ticker = tickerLayout.tickerTV.text
                        val str = (tickerText.plus(ticker)).trim()
                        this@apply.setText(str)
                        this@apply.setSelection(str.length)
                        showButtonView?.visibility = View.VISIBLE
                    }
                }
            }
            if (contentText != null && contentText.isNotEmpty()) showButtonView?.visibility = View.VISIBLE
        }

        val btnText = attrsArray.getString(R.styleable.TickersView_buttonText)
        val btnTextColor = attrsArray.getColor(R.styleable.TickersView_buttonTextColor, Color.YELLOW)
        showButtonView = view.findViewById<Button>(R.id.btnShow).apply {
            text = btnText
            setTextColor(btnTextColor)
            setOnClickListener {
                contentTextView?.let { textView ->
                    this.isEnabled = textView.text.isEmpty()
                    val list = if (textView.text.isEmpty()) listOf()
                    else textView.text.toString().split(" ")
                    listener?.onShowAllClick(list)
                }
            }
        }

        clearButtonView = view.findViewById<AppCompatImageView>(R.id.clearTextBtn).apply {
            setOnClickListener {
                contentTextView?.text?.clear()
                tickerText = ""
                listener?.onContentCleared()
            }
        }

        attrsArray.recycle()
    }

    fun setListeners(listener: Listener) {
        this.listener = listener
    }

    fun setContentText(text: String) {
        contentTextView?.setText(text)
        if (text.isEmpty()) {
            showButtonView?.visibility = View.INVISIBLE
        }
    }

    fun <T>setAdapter(adapter: T) where T : ListAdapter?, T : Filterable? {
        contentTextView?.setAdapter(adapter)
    }

    var editable: Boolean = false
        get() {
            return when {
                clearButtonView?.visibility == View.VISIBLE &&
                        contentTextView?.isClickable == true &&
                        contentTextView?.isFocusable == true -> true
                else -> false
            }
        }
        set(value) {
            if (value) {
                clearButtonView?.visibility = View.VISIBLE
                contentTextView?.apply {
                    isClickable = true
                    isFocusable = true
                }
            }
            else {
                clearButtonView?.visibility = View.GONE
                contentTextView?.apply {
                    isClickable = false
                    isFocusable = false
                }
            }
            field = value
        }

    val contentText: String
        get() = contentTextView?.text.toString()

}