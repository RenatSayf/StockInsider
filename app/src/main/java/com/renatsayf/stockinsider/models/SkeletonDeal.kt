package com.renatsayf.stockinsider.models

class SkeletonDeal(override var ticker: String?) : IDeal {
    override var filingDateRefer: String? = ""
    override var tradeDate: String? = ""
    override var insiderName: String? = ""
    override var volumeStr: String? = ""

}
