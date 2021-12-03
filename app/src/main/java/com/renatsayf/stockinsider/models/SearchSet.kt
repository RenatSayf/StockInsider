package com.renatsayf.stockinsider.models

import com.renatsayf.stockinsider.db.RoomSearchSet
import java.io.Serializable

data class SearchSet(var searchName : String?): Serializable
{
    var ticker : String = ""
    var filingPeriod : String = ""
    var tradePeriod : String = ""
    var isPurchase : String = ""
    var isSale : String = ""
    var excludeDerivRelated : String = "1"
    var tradedMin : String = ""
    var tradedMax : String = ""
    var isOfficer : String = ""
    var isDirector : String = ""
    var isTenPercent : String = ""
    var groupBy : String = ""
    var sortBy : String = ""

}