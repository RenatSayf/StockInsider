package com.renatsayf.stockinsider.models

import java.util.*

data class Deal(val filingDate : Date,
                val tradeDate : Date,
                val ticker : String,
                val company : String,
                val insiderName : String,
                val insiderTitle : String,
                val tradeType : String,
                val price : String,
                val qty : Double,
                val owned : Double,
                val deltaOwn : String,
                val value : Double)
{}