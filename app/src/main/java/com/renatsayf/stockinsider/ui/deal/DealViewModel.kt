package com.renatsayf.stockinsider.ui.deal

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DealViewModel : ViewModel()
{
    private var _background = MutableLiveData<Drawable>().apply {
        value = background?.value
    }
    var background: LiveData<Drawable> = _background

    fun setLayOutBackground(drawable: Drawable)
    {
        _background.value = drawable
    }
}
