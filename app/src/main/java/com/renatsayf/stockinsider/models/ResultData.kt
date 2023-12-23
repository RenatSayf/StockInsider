@file:Suppress("UNCHECKED_CAST")

package com.renatsayf.stockinsider.models

sealed class ResultData<out T> {
    object Init: ResultData<Nothing>()
    data class Success<out T>(val data: T): ResultData<T>()
    data class Error(val message: String, val errorCode: Int = Int.MIN_VALUE): ResultData<Nothing>()

    fun onInit(function: () -> Unit) {
        if (this is Init) {
            function.invoke()
        }
    }

    fun<T> onSuccess(function: (T) -> Unit) {
        if (this is Success) {
            function.invoke(this.data as T)
        }
    }

    fun onError(function: (String, Int) -> Unit) {
        if (this is Error) {
            function.invoke(this.message, this.errorCode)
        }
    }
}

