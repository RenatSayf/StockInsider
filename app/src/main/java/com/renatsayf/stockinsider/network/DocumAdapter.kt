package com.renatsayf.stockinsider.network

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

class DocumAdapter : Converter<ResponseBody, Document>
{
    @Throws(IOException::class)
    override fun convert(value : ResponseBody) : Document
    {
        return Jsoup.parse(value.string())
    }

    companion object
    {
        val FACTORY : Converter.Factory = object : Converter.Factory()
        {
            override fun responseBodyConverter(type : Type, annotations : Array<Annotation>, retrofit : Retrofit) : Converter<ResponseBody, *>?
            {
                return if (type === Document::class.java) DocumAdapter()
                else super.responseBodyConverter(type, annotations, retrofit)
            }
        }
    }
}