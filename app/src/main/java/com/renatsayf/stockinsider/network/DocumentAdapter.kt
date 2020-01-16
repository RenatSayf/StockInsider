package com.renatsayf.stockinsider.network

import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Converter

class DocumentAdapter : Converter<Response, Document>
{

    companion object
    {

    }

    override fun convert(value : Response) : Document
    {
        val document = Jsoup.parse(value.toString())
        return document
    }
}