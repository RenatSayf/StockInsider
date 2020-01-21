package com.renatsayf.stockinsider.models

data class Deal(val filingDate : String?)
{
    var filingDateRefer : String? = ""
    var tradeDate: String? = ""
    var ticker : String? = ""
    var tickerRefer : String? = ""
        set(value)
        {
            field = "https://www.profitspi.com/stock/stock-charts.ashx?chart=$value"
        }
    var company : String? = ""
    var companyRefer : String? = ""
        set(value)
        {
            field = "http://openinsider.com/$value"
        }
    var insiderName : String? = ""
    var insiderNameRefer : String? = ""
        set(value)
        {
            field = "http://openinsider.com$value"
        }
    var insiderTitle : String? = ""
    var tradeType : String? = ""
    var price : String? = ""
    var qty : Double? = 0.0
    var owned : Double? = 0.0
    var deltaOwn : String? = ""
    var volume : Double? = 0.0
}