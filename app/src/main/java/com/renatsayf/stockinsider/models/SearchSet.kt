package com.renatsayf.stockinsider.models

data class SearchSet(var searchName : String?)
{
    var companyName : String = ""
    var ticker : String = ""
    var filingPeriod : Int = 0
    var tradePeriod : Int = 0
}