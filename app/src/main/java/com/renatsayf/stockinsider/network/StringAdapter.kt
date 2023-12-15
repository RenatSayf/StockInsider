package com.renatsayf.stockinsider.network

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class StringAdapter : Converter<ResponseBody, String> {

    companion object
    {
        val FACTORY : Converter.Factory = object : Converter.Factory()
        {
            override fun responseBodyConverter(type : Type, annotations : Array<Annotation>, retrofit : Retrofit) : Converter<ResponseBody, *>?
            {
                return if (type === String::class.java) StringAdapter()
                else super.responseBodyConverter(type, annotations, retrofit)
            }
        }
    }
    override fun convert(value: ResponseBody): String {
        return value.string()
    }
}