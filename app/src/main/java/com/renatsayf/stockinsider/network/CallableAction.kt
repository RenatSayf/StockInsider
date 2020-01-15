package com.renatsayf.stockinsider.network

import com.renatsayf.stockinsider.models.SearchRequest
import org.jsoup.nodes.Document
import java.util.concurrent.Callable

class CallableAction constructor(private val searchRequest : SearchRequest) : Callable<Document>
{
    override fun call() : Document
    {
        return searchRequest.getTradeDocument()
    }
}