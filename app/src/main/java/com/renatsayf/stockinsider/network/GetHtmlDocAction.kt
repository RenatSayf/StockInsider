package com.renatsayf.stockinsider.network

import org.jsoup.nodes.Document
import java.util.concurrent.Callable

class GetHtmlDocAction constructor(private val searchRequest : SearchRequest) : Callable<Document>
{
    override fun call() : Document
    {
        return searchRequest.getTradeDocument()
    }
}