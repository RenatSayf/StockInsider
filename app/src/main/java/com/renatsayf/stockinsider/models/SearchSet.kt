package com.renatsayf.stockinsider.models

data class SearchSet(var searchName : String?)
{
    var ticker : String = ""
    var filingPeriod : String = ""
    var tradePeriod : String = ""
    var isPurchase : String = ""
    var isSale : String = ""
    var tradedMin : String = ""
    var tradedMax : String = ""
    var isOfficer : String = ""
    var isDirector : String = ""
    var isTenPercent : String = ""
    var groupBy : String = ""
    var sortBy : String = ""
}