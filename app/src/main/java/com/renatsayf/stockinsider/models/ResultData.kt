package com.renatsayf.stockinsider.models

sealed class ResultData<out T: Any> {
    object Init: ResultData<Nothing>()
    data class Success<out T: Any>(val data: T): ResultData<T>()
    data class Error(val message: String): ResultData<Nothing>()
}
