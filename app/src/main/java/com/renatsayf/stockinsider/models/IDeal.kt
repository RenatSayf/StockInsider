package com.renatsayf.stockinsider.models

interface IDeal {
    var filingDateRefer: String?
    var tradeDate: String?
    var ticker: String?
    var insiderName: String?
    var volumeStr: String?
}