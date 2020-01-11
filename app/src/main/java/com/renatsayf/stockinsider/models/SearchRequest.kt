package com.renatsayf.stockinsider.models

data class SearchRequest(val ticker: String)
{
    var filingDate: String = "fd="
    var tradeDate: String = "td="
    var isPurchase: String = "xp="
    var isSale: String = "xs="
    var tradedMin: String = "vl="
    var tradedMax: String = "vh="
    var isOfficer: String = "isofficer=1&iscob=1&isceo=1&ispres=1&iscoo=1&iscfo=1&isgc=1&isvp=1"
    var isDirector: String = "isdirector="
    var isTenPercent: String = "istenpercent="
    var groupBy: String = "grp="
    var sortBy: String = "sortcol="


}